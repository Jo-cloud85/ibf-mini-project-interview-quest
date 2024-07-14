package ibf_tfip.mini_project.backend.models;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionSummary {

    private String questionId;
    private String questionType;
    private String question;

    public String convertToJsonStr() {
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("questionId", questionId)
            .add("questionType", questionType)
            .add("question", question)
            .build();
        return jsonObject.toString();
    }
}