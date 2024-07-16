package ibf_tfip.mini_project.backend.controllers;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ibf_tfip.mini_project.backend.configs.SecurityConfig;
import ibf_tfip.mini_project.backend.exceptions.NoSubThreadFoundException;
import ibf_tfip.mini_project.backend.models.MainThread.ThreadContent;
import ibf_tfip.mini_project.backend.models.Question;
import ibf_tfip.mini_project.backend.models.QuestionSummary;
import ibf_tfip.mini_project.backend.services.FirebaseService;
import ibf_tfip.mini_project.backend.utils.HashUtils;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

@RestController
@RequestMapping("/api/qnsthread")
public class QnsThreadController {

    // For getting threads/responses between user and AI from Firebase Realtime Database
    // The creating part is in OpenAIAssistantController

    @Autowired
    private FirebaseService firebaseSvc;

    @Autowired
    private SecurityConfig securityConfig;


    @GetMapping("/{custom-job-id}/all-questions")
    public ResponseEntity<String> getAllQuestions(
        @PathVariable("custom-job-id") String customJobId,
        @RequestParam String firebaseThreadKey) throws IOException {

        // Authenticate user first
        String userId = securityConfig.getAuthenticatedUserId();

        String aiThreadContent = null;
        
        try {
            List<ThreadContent> tcList = firebaseSvc.getFirstSubThreadFrFB(userId, firebaseThreadKey);

            for (ThreadContent tc : tcList) {
                if (tc.getRole().equals("assistant")) {
                    aiThreadContent = tc.getContent();
                }
            }

            // Strip away unwanted characters or formatting issues using regex
            Pattern pattern = Pattern.compile("```json([\\s\\S]*?)```");
            Matcher matcher = pattern.matcher(aiThreadContent);

            if (!matcher.find()) {
                throw new IllegalArgumentException("Invalid input string");
            }

            String aiResponseJsonStr = matcher.group(1).trim();

            // Save formatted interview questions to Firestore
            List<Question> qnsList = convertToQuestionList(aiResponseJsonStr);
            firebaseSvc.saveInterviewQnsToFS(userId, qnsList, customJobId);

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(convertQnsListToJsonStr(qnsList));

        } catch (NoSubThreadFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + e.getMessage());
        }
    }   


    @GetMapping("/{custom-job-id}/{question-id}")
    public ResponseEntity<String> getQuestionByQnsId(
        @PathVariable("custom-job-id") String customJobId,
        @PathVariable("question-id") String qnsId) throws IOException, ExecutionException, InterruptedException {

        // Authenticate user first
        String userId = securityConfig.getAuthenticatedUserId();

        try {
            Question question = firebaseSvc.getInterviewQnsByQnsIdFrFS(userId, customJobId, qnsId);
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(question.convertToJsonStrWithId());
                
        } catch (Exception e) {
            JsonObject jsonObj = Json.createObjectBuilder()
                .add("error_message", "An unexpected error occured: " + e.getMessage())
                .build();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(jsonObj.toString());
        }
    }


    @GetMapping("/{custom-job-id}/all-attempted-questions")
    public ResponseEntity<String> getAllAttemptedQuestions(
        @PathVariable("custom-job-id") String customJobId,
        @RequestParam String firebaseThreadKey) throws IOException {

        // Authenticate user first
        String userId = securityConfig.getAuthenticatedUserId();
        
        try {
            List<QuestionSummary> questionSummList = firebaseSvc.getListOfQuestionSummaryFrFB(userId, firebaseThreadKey);

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(convertQnsSummListToJsonStr(questionSummList));
        
        } catch (NoSubThreadFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + e.getMessage());
        }
    }
    

    @GetMapping("/{custom-job-id}/{question-id}/question-thread")
    public ResponseEntity<String> getAttemptedQuestionSubThreadByQnsId(
        @PathVariable("custom-job-id") String customJobId,
        @PathVariable("question-id") String qnsId,
        @RequestParam String firebaseThreadKey) throws IOException, ExecutionException, InterruptedException {

        // Authenticate user first
        String userId = securityConfig.getAuthenticatedUserId();

        try {
            List<ThreadContent> qnsSubThreadContents = firebaseSvc.getQuestionSubThreadContentsByQnsIdFrFB(userId, firebaseThreadKey, qnsId);

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(convertThreadContentListToJsonStr(qnsSubThreadContents));
        } catch (NoSubThreadFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(e.getMessage());
        } catch (Exception e) {
            JsonObject jsonObj = Json.createObjectBuilder()
                .add("error_message", "An unexpected error occured: " + e.getMessage())
                .build();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(jsonObj.toString());
        }
    }

    // When you delete, you have to delete from S3, Firebase and Assistant, Vector, everything...
    // @DeleteMapping("/custom-job/{customJobId}")
    // public ResponseEntity<String> deleteCustomJob (@PathVariable String customJobId) {
        
    // }


    //// HELPER METHODS ////////////////////////////////////////////////////////////////////
    private List<Question> convertToQuestionList(String jsonStr) {
        List<Question> qnsList = new ArrayList<>();

        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonStr))) {
            JsonObject jsonObj = jsonReader.readObject();
            JsonArray qnsArray = jsonObj.getJsonArray("all_questions");

            for (JsonValue value : qnsArray) {
                JsonObject qnsObj = value.asJsonObject();
                String questionType = qnsObj.getString("question_type");
                String question = qnsObj.getString("question");
                String suggestedAnswerApproach = qnsObj.getString("suggested_answer_approach");

                // Don't use UUID, else the questionId keeps changing everytime you fetch all questions
                String questionId =  HashUtils.generateHash(question);

                Question newQuestion = new Question(questionId, questionType, question, suggestedAnswerApproach);
                qnsList.add(newQuestion);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return qnsList;
    }


    private static String convertQnsListToJsonStr(List<Question> questions) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Question question : questions) {
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                .add("questionId", question.getQuestionId())
                .add("questionType", question.getQuestionType())
                .add("question", question.getQuestion())
                .add("answerApproach", question.getSuggestedAnswerApproach());

            jsonArrayBuilder.add(jsonObjectBuilder);
        }

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("all_questions", jsonArrayBuilder)
            .build();

        return jsonObject.toString();
    }


    private static String convertQnsSummListToJsonStr(List<QuestionSummary> questions) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (QuestionSummary question : questions) {
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                .add("questionId", question.getQuestionId())
                .add("questionType", question.getQuestionType())
                .add("question", question.getQuestion());
            jsonArrayBuilder.add(jsonObjectBuilder);
        }

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("all_questionSummaries", jsonArrayBuilder)
            .build();

        return jsonObject.toString();
    }


    private static String convertThreadContentListToJsonStr(List<ThreadContent> threadContents) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (ThreadContent tc : threadContents) {
            JsonObjectBuilder tcJsonObj = Json.createObjectBuilder()
                .add("timestamp", tc.getTimestamp())
                .add("role", tc.getRole())
                .add("content", tc.getContent());
            jsonArrayBuilder.add(tcJsonObj);
        }

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("all_threadContents", jsonArrayBuilder)
            .build();

        return jsonObject.toString();
    }
}
