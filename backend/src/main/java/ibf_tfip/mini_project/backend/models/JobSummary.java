package ibf_tfip.mini_project.backend.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobSummary {

    private String userId;
    private String customJobId;
    private String assistantId;
    private String threadId;
    private String firebaseThreadKey;
    private String title;
    private String level;
    private LocalDateTime createdTime;
    private boolean isAttempted; //whether user has attempted question

    public JsonObjectBuilder convertToJson(JobSummary job) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder()
                    .add("userId", job.getCustomJobId())
                    .add("customJobId", job.getCustomJobId())
                    .add("assistantId", job.getAssistantId())
                    .add("threadId", job.getThreadId())
                    .add("firebaseThreadKey", job.getFirebaseThreadKey())
                    .add("title", job.getTitle())
                    .add("level", job.getLevel())
                    .add("createdTime", job.getCreatedTime().format(formatter))
                    .add("isAttempted", job.isAttempted());
        return jsonObjBuilder;
    }
}
