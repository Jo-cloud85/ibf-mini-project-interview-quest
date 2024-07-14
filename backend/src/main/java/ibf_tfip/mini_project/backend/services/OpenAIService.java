package ibf_tfip.mini_project.backend.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import ibf_tfip.mini_project.backend.models.MainThread;
import ibf_tfip.mini_project.backend.models.MainThread.QuestionSubThread;
import ibf_tfip.mini_project.backend.models.MainThread.ThreadContent;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;


// The service here is to create an assistant for every job created.
// https://platform.openai.com/docs/assistants/tools/file-search/quickstart

@Service
public class OpenAIService {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.model}")
    private String apiModel;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.assistant.version}")
    private String apiAssistantVersion;


    // 1. Create a new Assistant with File Search Enabled -------------------------------------------------------
    // https://platform.openai.com/docs/api-reference/assistants/createAssistant
    // POST https://api.openai.com/v1/assistants
    /*
        Expected response from this method - An assistant object in string BUT we only want the assistant id
        {
            "id": "asst_abc123",
            "object": "assistant",
            "created_at": 1698984975,
            "name": "Math Tutor",
            "description": null,
            "model": "gpt-4-turbo",
            "instructions": "You are a personal math tutor. When asked a question, write and run Python code to answer the question.",
            "tools": [
                { "type": "file_search" }
            ],
            "metadata": {},
            "top_p": 1.0,
            "temperature": 1.0,
            "response_format": "auto"
        }
     */
    public String createAssistant() {

        String instructions = """
            You are an job interview expert assistant. Your main role is to help users to ace their next job interview, both behaviourial and technical aspects, if relevant. 
            You have the knowledge base of interview questions that are commonly asked and also the knowledge to answer 
            them in a professional concise manner that will impress interviewers. 
            Use the resumes or files uploaded to tailor the interview questions and answers towards each user/job role.
            If the job level of the role is an internship or entry-level, make sure you tailor your questions to match it.
        """;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        // Requirement by OpenAI - https://platform.openai.com/docs/assistants/overview
        headers.set("OpenAI-Beta", apiAssistantVersion);

        JsonArray toolsJsonArr = Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                    .add("type", "file_search")
                    .build())
            .build();

        JsonObject jsonObj = Json.createObjectBuilder()
            .add("name", "InterviewQuest")
            .add("instructions", instructions)
            .add("model", apiModel)
            .add("tools", toolsJsonArr)
            .build();

        String assistantBody = jsonObj.toString();
        HttpEntity<String> requestEntity = new HttpEntity<>(assistantBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
            apiUrl + "/v1/assistants",
            requestEntity,
            String.class
        );

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonObject assistantResponse = Json.createReader(new StringReader(responseEntity.getBody())).readObject();
            System.out.println("Assistant created");
            return assistantResponse.getString("id");
        } else {
            throw new RuntimeException("Failed to create assistant. Status code: " + responseEntity.getStatusCode());
        }
    }


    // 2 Upload files and add them to a Vector Store ------------------------------------------------------------
    // 2.1 Upload files and get file-ids
    // https://platform.openai.com/docs/api-reference/files/create
    // POST https://api.openai.com/v1/files
    private List<String> uploadFiles(List<ByteArrayResource> byteResources) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("OpenAI-Beta", apiAssistantVersion);

        List<String> fileIds = new ArrayList<>();

        for (ByteArrayResource byteR : byteResources) {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", byteR);
            body.add("purpose", "assistants");
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            //HttpEntity<String> requestEntity = new HttpEntity<>(fileBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                apiUrl + "/v1/files", 
                requestEntity, 
                String.class);
            
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                JsonObject fileResponse = Json.createReader(new StringReader(responseEntity.getBody())).readObject();
                fileIds.add(fileResponse.getString("id"));
                
            } else {
                throw new RuntimeException("Failed to upload files. Status code: " + responseEntity.getStatusCode());
            }
        } 
        System.out.println("Files uploaded");
        return fileIds;
    }

    // 2.2 Create a Vector Store
    // https://platform.openai.com/docs/api-reference/vector-stores
    // POST https://api.openai.com/v1/vector_stores
    /*
        Expected response from this method - An vector store object in string BUT we only want the vector store id
        {
            "id": "vs_abc123",
            "object": "vector_store",
            "created_at": 1699061776,
            "name": "Support FAQ",
            "bytes": 139920,
            "file_counts": {
                "in_progress": 0,
                "completed": 3,
                "failed": 0,
                "cancelled": 0,
                "total": 3
            }
        }
     */
    public String uploadFilesToVectorStore(List<ByteArrayResource> resources) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("OpenAI-Beta", apiAssistantVersion);

        List<String> fileIds = uploadFiles(resources);

        JsonObject jsonObj = Json.createObjectBuilder()
            .add("name", "Job Documentation")
            .add("file_ids", Json.createArrayBuilder(fileIds))
            .build();

        String vectorStoreBody = jsonObj.toString();
        HttpEntity<String> requestEntity = new HttpEntity<>(vectorStoreBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
            apiUrl + "/v1/vector_stores", 
            requestEntity, 
            String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonObject vectorStoreResponse = Json.createReader(new StringReader(responseEntity.getBody())).readObject();
            System.out.println("Vector store created and uploaded files added to vector store");
            return vectorStoreResponse.getString("id");
        } else {
            throw new RuntimeException("Failed to create a vector store with the uploaded files. Status code: " + responseEntity.getStatusCode());
        }
    }


    // 3. Update the assistant to use the new Vector Store ------------------------------------------------------
    // https://platform.openai.com/docs/api-reference/assistants/modifyAssistant
    // POST https://api.openai.com/v1/assistants/{assistant_id}
    /*
        Expected response but you need not return it.
        {
            "id": "asst_123",
            "object": "assistant",
            "created_at": 1699009709,
            "name": "HR Helper",
            "description": null,
            "model": "gpt-4-turbo",
            "instructions": "You are an HR bot, and you have access to files to answer employee questions about company policies. Always response with info from either of the files.",
            "tools": [
                { "type": "file_search"}
            ],
            "tool_resources": {
                "file_search": {
                "vector_store_ids": []
                }
            },
            "metadata": {},
            "top_p": 1.0,
            "temperature": 1.0,
            "response_format": "auto"
        }
     */
    private String updateAssistantToolResources(String assistantId, String vectorStoreId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("OpenAI-Beta", apiAssistantVersion);

        JsonArray vectorStoreArr = Json.createArrayBuilder()
            .add(vectorStoreId)
            .build();

        JsonObject vectorStoreObj = Json.createObjectBuilder()
            .add("vector_store_ids", vectorStoreArr)
            .build();

        JsonObject fileSearchObj = Json.createObjectBuilder()
            .add("file_search", vectorStoreObj)
            .build();

        JsonObject jsonObj = Json.createObjectBuilder()
            .add("tool_resources", fileSearchObj)
            .build();

        String updateBody = jsonObj.toString();
        HttpEntity<String> requestEntity = new HttpEntity<>(updateBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
            apiUrl + "/v1/assistants/" + assistantId, 
            requestEntity,
            String.class,
            assistantId);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonObject assistantResponse = Json.createReader(new StringReader(responseEntity.getBody())).readObject();
            System.out.println("Assistant updated");
            return assistantResponse.toString();
        } else {
            throw new RuntimeException("Failed to update assistant with tool resources. Status code: " + responseEntity.getStatusCode());
        }
    }


    // 4. Create a Thread and Run -------------------------------------------------------------------------------
    //  - With an initial messages and updated assistant and then run 
    // https://platform.openai.com/docs/assistants/tools/file-search/step-4-create-a-thread
    // https://platform.openai.com/docs/api-reference/runs/createThreadAndRun
    // POST https://api.openai.com/v1/threads/runs
    /*
        Since we are using stream:
        Expected response from this method - A thread object but we want the event where thread.message.complete

        event: thread.created
        data: {
            "id":"thread_123",
            "object":"thread",
            "created_at":1710348075,
            "metadata":{}
        }

        ...

        event: thread.message.delta
        data: {
            "id":"msg_001",
            "object":"thread.message.delta",
            "delta":{
                    "content":[
                        {
                            "index":0,
                            "type":"text",
                            "text":{"value":"?"}
                        }
                    ]
                }
            }

        event: thread.message.completed
        data: {
            "id":"msg_001",
            "object":"thread.message",
            "created_at":1710348076,
            "assistant_id":"asst_123",
            "thread_id":"thread_123",
            "run_id":"run_123",
            "status":"completed",
            "incomplete_details":null,
            "incomplete_at":null,
            "completed_at":1710348077,
            "role":"assistant",
            "content":[
                {
                    "type":"text",
                    "text":{
                        "value":"Hello! How can I assist you today?",
                        "annotations":[]
                    }
                }
            ], 
            "metadata":{}
        }

        ...

        event: done
        data: [DONE]
     */
    // Added parameters to accept data in OpenAIController
    public MainThread createThreadAndRun(
        String vectorStoreId, 
        String messageContent,
        String customJobId,
        String assistantId) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("OpenAI-Beta", apiAssistantVersion);

        // Update assistant
        updateAssistantToolResources(assistantId, vectorStoreId);

        JsonObjectBuilder messageBuilder = Json.createObjectBuilder()
                .add("role", "user")
                .add("content", messageContent); //these will be the rest of the requestparts from Angular form data

        JsonArrayBuilder messagesBuilder = Json.createArrayBuilder().add(messageBuilder.build());
        JsonArray messagesArray = messagesBuilder.build();

        JsonObject threadPayloadObj = Json.createObjectBuilder()
                .add("messages", messagesArray)
                .build();

        JsonObject runPayloadObj = Json.createObjectBuilder()
                .add("assistant_id", assistantId)
                .add("thread", threadPayloadObj)
                .add("stream", true)
                .build();

        String runPayload = runPayloadObj.toString();

        // System.out.println("Run Payload:\n" + runPayload);

        HttpEntity<String> requestEntity = new HttpEntity<>(runPayload, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
            apiUrl + "/v1/threads/runs", 
            requestEntity, 
            String.class);  

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String responseBody = responseEntity.getBody();

            // System.out.println("Response Body" + responseBody);
            
            BufferedReader reader = new BufferedReader(new StringReader(responseBody));
            String line;
            String userTimestamp = null; //when thread is created
            String threadId = null;
            String assistantTimestamp = null; //when thread is complete
            String responseFrAI = null;

            MainThread mainThread = new MainThread();

            try {
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("event: thread.message.completed")) {
                        // Read the next line for the data
                        String dataLine = reader.readLine();
                        if (dataLine != null && dataLine.startsWith("data: ")) {
                            String jsonData = dataLine.substring(6); // Remove the "data: " prefix
                            JsonObject dataObj = Json.createReader(new StringReader(jsonData)).readObject();
                            
                            // Must follow the order of responseBody as we are reading line by line
                            // created_at
                            Long createdAtUnix = dataObj.getJsonNumber("created_at").longValue();
                            userTimestamp = Long.toString(createdAtUnix);

                            threadId = dataObj.getString("thread_id");

                            // completed_at
                            Long completedAtUnix = dataObj.getJsonNumber("completed_at").longValue();
                            assistantTimestamp = Long.toString(completedAtUnix);

                            // content
                            responseFrAI = dataObj.getJsonArray("content")
                                                .getJsonObject(0)
                                                .getJsonObject("text")
                                                .getString("value");
                                                
                            System.out.println("Almost there...");
                            break;
                        }
                    }
                }

                // Initialize 
                mainThread.setCustomJobId(customJobId);
                mainThread.setAssistantId(assistantId);
                mainThread.setThreadId(threadId);
                ThreadContent user = new ThreadContent(userTimestamp, "user", messageContent);
                ThreadContent assistant = new ThreadContent(assistantTimestamp, "assistant", responseFrAI);
                List<ThreadContent> firstSubThread = new ArrayList<>();
                firstSubThread.add(user);
                firstSubThread.add(assistant);
                mainThread.setFirstSubThread(firstSubThread);

            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Main Thread created!");
            return mainThread;

        } else {
            throw new RuntimeException("Failed to create a thread and run it. Status code: " + responseEntity.getStatusCode());
        }
    }


    // 5. Create Run (this for adding new messages to the thread and running it i.e. user sends new input, AI replies)
    // https://platform.openai.com/docs/api-reference/runs/createRun
    // POST https://api.openai.com/v1/threads/{thread_id}/runs
    public QuestionSubThread createNewRun(
        String customJobId,
        String assistantId,
        String threadId,
        String addInstructions,
        String addMessages,
        String questionId,
        String questionType,
        String question) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("OpenAI-Beta", apiAssistantVersion);

        // For additional messages, OpenAI only accepts array

        JsonObjectBuilder addMessageBuilder = Json.createObjectBuilder()
            .add("role", "user")
            .add("content", addMessages); //these will be the rest of the requestparts from Angular form data

        JsonArrayBuilder messagesBuilder = Json.createArrayBuilder()
            .add(addMessageBuilder.build());
        JsonArray messagesArray = messagesBuilder.build();

        JsonObject runPayloadObj = Json.createObjectBuilder()
            .add("assistant_id", assistantId)
            .add("additional_instructions", addInstructions)
            .add("additional_messages", messagesArray)
            .add("stream", true)
            .build();

        String runPayload = runPayloadObj.toString();

        // System.out.println("Run Payload:\n" + runPayload);

        HttpEntity<String> requestEntity = new HttpEntity<>(runPayload, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
            apiUrl + "/v1/threads/" + threadId + "/runs", 
            requestEntity, 
            String.class,
            threadId);  

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String responseBody = responseEntity.getBody();

            // System.out.println("Response Body" + responseBody);
            
            BufferedReader reader = new BufferedReader(new StringReader(responseBody));
            String line;
            String userTimestamp = null; //when thread is created
            String assistantTimestamp = null; //when thread is complete
            String responseFrAI = null;

            QuestionSubThread qnsThread = new QuestionSubThread();

            try {
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("event: thread.message.completed")) {
                        // Read the next line for the data
                        String dataLine = reader.readLine();
                        if (dataLine != null && dataLine.startsWith("data: ")) {
                            String jsonData = dataLine.substring(6); // Remove the "data: " prefix
                            JsonObject dataObj = Json.createReader(new StringReader(jsonData)).readObject();
                            
                            // Must follow the order of responseBody as we are reading line by line
                            // created_at
                            Long createdAtUnix = dataObj.getJsonNumber("created_at").longValue();
                            userTimestamp = Long.toString(createdAtUnix);

                            // completed_at
                            Long completedAtUnix = dataObj.getJsonNumber("completed_at").longValue();
                            assistantTimestamp = Long.toString(completedAtUnix);

                            // content
                            responseFrAI = dataObj.getJsonArray("content")
                                                .getJsonObject(0)
                                                .getJsonObject("text")
                                                .getString("value");         
                            break;
                        }
                    }
                }

                // Initialize 
                ThreadContent user = new ThreadContent(userTimestamp, "user", addMessages);
                ThreadContent assistant = new ThreadContent(assistantTimestamp, "assistant", responseFrAI);
                List<ThreadContent> qnsSubThreadContent = new ArrayList<>();
                qnsSubThreadContent.add(user);
                qnsSubThreadContent.add(assistant);

                qnsThread.setQuestionId(questionId);
                qnsThread.setQuestionType(questionType);
                qnsThread.setQuestion(question);
                qnsThread.setThreadContents(qnsSubThreadContent);

            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Question sub thread created!");
            return qnsThread;

        } else {
            throw new RuntimeException("Failed to create question thread and run it. Status code: " + responseEntity.getStatusCode());
        }
    }
}