package ibf_tfip.mini_project.backend.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import ibf_tfip.mini_project.backend.configs.SecurityConfig;
import ibf_tfip.mini_project.backend.models.JobDetails;
import ibf_tfip.mini_project.backend.models.JobSummary;
import ibf_tfip.mini_project.backend.models.MainThread;
import ibf_tfip.mini_project.backend.models.Question;
import ibf_tfip.mini_project.backend.services.EmailService;
import ibf_tfip.mini_project.backend.services.FirebaseService;
import ibf_tfip.mini_project.backend.services.JobSummaryService;
import ibf_tfip.mini_project.backend.services.OpenAIService;
import ibf_tfip.mini_project.backend.services.S3Service;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import ibf_tfip.mini_project.backend.models.MainThread.QuestionSubThread;
import ibf_tfip.mini_project.backend.models.MainThread.ThreadContent;

@RestController
@RequestMapping("/api/ai")
public class OpenAIController {

    @Autowired
    private OpenAIService openAISvc;

    @Autowired
    private S3Service s3Svc;

    @Autowired
    private FirebaseService firebaseSvc;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private EmailService emailSvc;

    @Autowired
    private JobSummaryService jobSummarySvc;

    private final Integer numberOfQns = 5;

    private final String baseUrl = "https://zippy-strength-production.up.railway.app/";

    // Each custom job created should have:
    // 1 custom job id, 1 assistant id, 1 vector id, 1 thread id (mainthread), multiple run ids 

    //// Create job and first AI response ///////////////////////////////////////////
    @PostMapping("/create-job")
    public ResponseEntity<String> createNewCustomJob(
        @RequestPart String jobTitle, 
        @RequestPart String jobDescription,
        @RequestPart String jobLevel, 
        @RequestPart(required=false) String companyName,
        @RequestPart(required=false) String companyDetails,
        @RequestPart MultipartFile[] documents) throws Exception {

        System.out.println("Receiving form from frontend...");

        // Authenticate user first
       
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof String)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authenticated or token is invalid");
        }
        String userId = (String) authentication.getPrincipal();
        String username = authentication.getName();
        String email = (String) authentication.getCredentials();

        // Create custom job id
        String customJobId = UUID.randomUUID().toString().substring(0, 8);
        
        // Retrieve folder name to get files from S3
        String folderName = saveAndRetrieveFolderNameFrS3(documents, customJobId);
        List<ByteArrayResource> resources = s3Svc.getFilesFrS3(folderName);

        // Construct message for Assistant AI to generate thread
        String messageContent = constructMessageContent(
            jobTitle, jobDescription, jobLevel, companyName, companyDetails);

        MainThread mainThread = createAssistantAndRunProcesses(
            resources, messageContent, customJobId); // Here, the userId is still null
        mainThread.setUserId(userId);

        // Get thread
        List<ThreadContent> firstThreadContent = mainThread.getFirstSubThread();
        for (ThreadContent content : firstThreadContent) {
            if (content.getRole().equals("assistant")) {
                String aiResponse = content.getContent();
                if (aiResponse != null) {
                    // Save the firstSubThread into Firebase Realtime Database
                    String firebaseThreadKey = firebaseSvc.saveFirstThreadToFB(
                        userId,
                        mainThread.getFirstSubThread(), 
                        customJobId, 
                        mainThread.getAssistantId(),
                        mainThread.getThreadId());

                    // Save job summary to MySQL
                    JobSummary jobSumm = new JobSummary(
                        userId,
                        customJobId, 
                        mainThread.getAssistantId(), 
                        mainThread.getThreadId(), 
                        firebaseThreadKey,
                        jobTitle, 
                        jobLevel != null ? jobLevel.toUpperCase() : null, 
                        LocalDateTime.now(), 
                        false);
                    boolean isAdded = jobSummarySvc.addNewJobSummary(jobSumm);

                    // If successfully saved
                    if (folderName != null && firebaseThreadKey != null && isAdded) {

                        // Send email to user
                        String subject = "Custom job created!";
                        String body = emailBody(username, jobTitle, baseUrl);
                        emailSvc.sendEmail(email, subject, body);

                        // Send response to frontend
                        JsonObject jsonObject = Json.createObjectBuilder()
                            .add("message", "Request to create new custom job successful!")
                            .build();          
                        return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(jsonObject.toString());

                    } else {
                        JsonObject jsonObject = Json.createObjectBuilder()
                            .add("error_message", "Error saving data to Firebase and MySQL")
                            .build();
                        return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(jsonObject.toString());
                    }
                } else {
                    JsonObject jsonObject = Json.createObjectBuilder()
                        .add("error_message", "Seems like AI did not respond")
                        .build();
                    return ResponseEntity
                        .status(HttpStatus.EXPECTATION_FAILED)
                        .body(jsonObject.toString());
                }
            }
        }

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("error_message", "An unexpected error occurred")
                .build();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(jsonObject.toString());
    }


    //// Create feedback/answer for single question ///////////////////////////////////////////
    @PostMapping("/{custom-job-id}/{question-id}/request-feedback")
    public ResponseEntity<String> requestFeedback(
        @PathVariable("custom-job-id") String customJobId,
        @PathVariable("question-id") String questionId,
        @RequestBody String userPayload) throws InterruptedException, ExecutionException {

        System.out.println("Receiving userInput from frontend...");

        // Authenticate user first
        String userId = securityConfig.getAuthenticatedUserId();

        try {
            // Using custom job id to retrieve job summary data from MySQL
            JobSummary jobSumm = jobSummarySvc.getJobSummaryByCustomJobId(customJobId);

            // Using userId, customJobId, and questionId to retrieve a single question
            Question qns = firebaseSvc.getInterviewQnsByQnsIdFrFS(userId, customJobId, questionId);

            // Creating question sub-thread with AI
            QuestionSubThread qnsSubThread = this.openAISvc.createNewRun(
                customJobId,
                jobSumm.getAssistantId(),
                jobSumm.getThreadId(),
                addInstructions(),
                addMessages(qns, userPayload),
                qns.getQuestionId(),
                qns.getQuestionType(),
                qns.getQuestion()
            );

            // Reformat the qnsSubThread threadContents before saving to Firebase Realtime Database
            String formattedAIResponse = null;
            Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");

            for (ThreadContent tc : qnsSubThread.getThreadContents()) {
                if ("assistant".equals(tc.getRole())) {
                    Matcher matcher = pattern.matcher(tc.getContent());
                    if (matcher.find()) {
                        formattedAIResponse = matcher.group(1).trim();
                        tc.setContent(formattedAIResponse);
                    } else {
                        throw new IllegalArgumentException("Invalid input string");
                    }
                }

                if ("user".equals(tc.getRole())) {
                    tc.setContent(userPayload);
                }
            }

            // Save qnsSubThread to Firebase Realtime Database
            firebaseSvc.saveQnsThreadToFB(
                userId,
                qnsSubThread,
                jobSumm.getFirebaseThreadKey());

            JsonObject jsonObj = Json.createObjectBuilder()
                .add("ai_response", formattedAIResponse)
                .build();

            System.out.println("Replying to user...");

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(jsonObj.toString());

        } catch (Exception e) {
            JsonObject jsonObj = Json.createObjectBuilder()
                .add("error_message", "An unexpected error occurred: " + e.getMessage())
                .build();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(jsonObj.toString());
        }
    }



    //// HELPER METHODS for POST createJob //////////////////////////////////////////////////////////
    private String saveAndRetrieveFolderNameFrS3(
        MultipartFile[] documents, 
        String customJobId) throws IOException {
            
        // Just need 1 url in the list of urls to get folder name
        String url = s3Svc.saveFilesToS3(documents, customJobId).get(0); 
        int startIndex = url.indexOf(".com/");
        int endIndex = url.lastIndexOf("/");
        return url.substring(startIndex + 5, endIndex);
    }
    
    private String constructMessageContent(
        String jobTitle, 
        String jobDescription, 
        String jobLevel,
        String companyName, 
        String companyDetails) {
    
        String messageStr = new JobDetails(
            jobTitle, jobDescription, jobLevel, companyName, companyDetails).toString();
            
        Question question = new Question(
            "Behavioural", "<question>", "<suggested_approach>");

        String questionJsonStr = question.convertToJsonStr();
    
        return "Based on the attached files and these additional details:\n" + 
               messageStr + "\n" +
               "Can you customise and generate at least " + 
               numberOfQns + " behavioral and " + 
               numberOfQns + " technical interview questions? \n" +
               "Format your response in JSON format under the key \"all_questions\", " +
               "where you have an array of json objects, each for a single question, \n" +
               "And each follows this format:\n" +
               questionJsonStr +
               "\nMake sure your difficulty of questions matches the job level." +
               "\nLastly, DO NOT output any additional text outside of the JSON";
    }
    
    private MainThread createAssistantAndRunProcesses(
        List<ByteArrayResource> resources, 
        String messageContent,
        String customJobId) throws Exception {

        String assistantId = openAISvc.createAssistant();
        String vectorStoreId = openAISvc.uploadFilesToVectorStore(resources);
        return openAISvc.createThreadAndRun(vectorStoreId, messageContent, customJobId, assistantId);
    }

    private String emailBody(String firstName, String jobTitle, String baseUrl) {
        String emailBody = 
                    "<html>" +
                    "<body>" +
                    "<h2>Hi " + firstName + ",</h2>" +
                    "<h3>Your interview questions for custom job: </h3>" +
                    "<h3>" + jobTitle + "</h3>" +
                    "<h3>are ready!</h3><br>" + 
                    "<p>Click on the button below to embark on your quest.</p>" +
                    "<p><a href='" + baseUrl + "/interview-quest/overview' style='display: inline-block; " +
                        "padding: 10px 20px; " +
                        "border: 2px solid white; " +
                        "border-radius: 8px; " +
                        "background-color: #FE7E35; " +
                        "color: #F7F6F4; " +
                        "text-decoration: none;'>" +  // Corrected here
                        "Have fun practicing!" +
                    "</a></p>" +
                    "<p>Best Regards,<br>InterviewQuest Team</p>" +
                    "</body>" +
                    "</html>";
        return emailBody;
    }


    //// HELPER METHODS for POST requestFeedback ////////////////////////////////////////////////////
    private String addInstructions(){
        return """
            Still keeping in mind, the user profile that you have understood from the uploaded files
            and custom job details submitted, the user is now trying to practice the interview questions 
            that you have generated for the user. There will be 2 scenarios coming from user:
            1. User request to 'Generate Answer'
                - This means you will propose an answer to the interview question that the user is
                  trying to practice.
                - Format your response in JSON format under the key \"suggested_answer\"
            2. User writes his/her answer for the interview question
                - Analyze user's input and give professional feedback on how the user can improve   
                  his/her answer. 
                - Format your response in JSON format under the key \"feedback\"
            Using your expertise and the well-known STAR method for behavioural questions especially, 
            to help the user. 
            Lastly, DO NOT output anymore questions.
        """;
    }

    private String addMessages(Question qns, String payload){
        return "Based on this question:\n " + 
                qns.getQuestion() + "\n" +  
                "and user's message: " + "\n" + 
                payload + ".\n" + 
                "Only respond with either feedback or suggested answer. DO NOT generate anymore questions.";
    }
}
