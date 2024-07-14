package ibf_tfip.mini_project.backend.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Mainly for frontend reasons

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {
    private String scheduleId; // aka eventId
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String url;

    public JsonObject convertToJsonObject() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
            .add("scheduleId", scheduleId != null ? scheduleId : "")
            .add("title", title != null ? title : "")
            .add("description", description != null ? description : "")
            .add("startDateTime", startDateTime != null ? startDateTime.format(formatter) : "")
            .add("endDateTime", endDateTime != null ? endDateTime.format(formatter) : "")
            .add("url", url != null ? url : "");

        return jsonBuilder.build();
    }
}
