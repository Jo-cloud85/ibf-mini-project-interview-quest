package ibf_tfip.mini_project.backend.models;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    private String questionId;
    private String questionType;
    private String question;
    private String suggestedAnswerApproach;

    public Question(String questionType, String question, String suggestedAnswerApproach) {
        this.questionType = questionType;
        this.question = question;
        this.suggestedAnswerApproach = suggestedAnswerApproach;
    }

    // W/o questionId
    public String convertToJsonStr() {
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("question_type", questionType)
            .add("question", question)
            .add("suggested_answer_approach", suggestedAnswerApproach)
            .build();
        return jsonObject.toString();
    }

    public String convertToJsonStrWithId() {
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("questionId", questionId)
            .add("questionType", questionType)
            .add("question", question)
            .add("answerApproach", suggestedAnswerApproach)
            .build();
        return jsonObject.toString();
    }
}
