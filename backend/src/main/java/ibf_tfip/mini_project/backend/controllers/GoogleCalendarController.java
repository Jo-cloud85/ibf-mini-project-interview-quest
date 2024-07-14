package ibf_tfip.mini_project.backend.controllers;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ibf_tfip.mini_project.backend.models.Schedule;
import ibf_tfip.mini_project.backend.services.GoogleCalendarService;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/calendar")
public class GoogleCalendarController {

    @Autowired
    private GoogleCalendarService googleCalendarSvc;

    // Create
    @PostMapping("/create-schedule")
    public ResponseEntity<String> createSchedule(
        @RequestBody String schedulePayload,
        HttpServletRequest request, 
        HttpServletResponse response) throws IOException, GeneralSecurityException {

        JsonReader jsonReader = Json.createReader(new StringReader(schedulePayload));
        JsonObject scheduleJsonObj = jsonReader.readObject();

        String description = scheduleJsonObj
            .isNull("description") ? "null" : scheduleJsonObj.getString("description");

        String eventLink = googleCalendarSvc.createSchedule(
            scheduleJsonObj.getString("title"),
            description,
            scheduleJsonObj.getString("startDateTime"),
            scheduleJsonObj.getString("endDateTime"),
            scheduleJsonObj.getString("rruleStr"),
            scheduleJsonObj.getString("email")
        );

        JsonObject jsonObj = Json.createObjectBuilder()
            .add("event_link", eventLink)
            .build();
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(jsonObj.toString());
    }


    // Read All
    @GetMapping("/all-schedule")
    public ResponseEntity<String> getAllSchedules() {

        try {
            Optional<List<Schedule>> scheduleList = googleCalendarSvc.getAllSchedules();

            if (scheduleList.isEmpty()) {
                JsonObject jsonObject = Json.createObjectBuilder()
                    .add("error_message", "No schedules available")
                    .build();
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(jsonObject.toString());
            }
            
            JsonArrayBuilder jsonArr = Json.createArrayBuilder();
            for (Schedule s: scheduleList.get()) {
                System.out.println(s.getScheduleId());
                JsonObject jsonObj = s.convertToJsonObject();
                jsonArr.add(jsonObj);
            }
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(jsonArr.build().toString());

        } catch (Exception e) {
            JsonObject jsonObject = Json.createObjectBuilder()
                .add("error_message", "An unexpected error occurred: " + e.getMessage())
                .build();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(jsonObject.toString());
        }
    }


    // Delete
    @DeleteMapping("/all-schedule/{scheduleId}/delete")
    public ResponseEntity<String> deleteSchedule(
        @PathVariable("scheduleId") String eventId) throws IOException, GeneralSecurityException {
        
        try {
            boolean isDeleted = googleCalendarSvc.deleteSchedule(eventId);
            if (isDeleted) {
                JsonObject jsonObject = Json.createObjectBuilder()
                    .add("success_message", "Schedule deleted!")
                    .build();
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(jsonObject.toString());
            }
        } catch (Exception e) {
            JsonObject jsonObject = Json.createObjectBuilder()
                .add("error_message", "An unexpected error occurred: " + e.getMessage())
                .build();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(jsonObject.toString());
        }
        return null;
    }
}
