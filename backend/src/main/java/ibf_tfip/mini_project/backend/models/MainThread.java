package ibf_tfip.mini_project.backend.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Main class representing the Thread
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainThread {
    private String userId;
    private String customJobId;
    private String assistantId;
    private String threadId;
    private List<ThreadContent> firstSubThread;
    private List<QuestionSubThread> qnsSubThreads = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionSubThread { 
        private String questionId;
        private String questionType;
        private String question;
        private List<ThreadContent> threadContents;

        public String convertToJsonStr() {
            JsonArrayBuilder jsonArr = Json.createArrayBuilder();
            for (ThreadContent tc : threadContents) {
                JsonObjectBuilder tcJsonObj = Json.createObjectBuilder()
                    .add("timestamp", tc.getTimestamp())
                    .add("role", tc.getRole())
                    .add("content", tc.getContent());
                jsonArr.add(tcJsonObj);
            }
            JsonObject jsonObject = Json.createObjectBuilder()
                .add("questionId", questionId)
                .add("questionType", questionType)
                .add("question", question)
                .add("threadContents", jsonArr.build())
                .build();
            return jsonObject.toString();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ThreadContent { // Make this static to be able to instantiate it without an outer class instance
        private String timestamp; // Firebase cannot store any form of Date
        private String role;
        private String content;
    }
}
