package ibf_tfip.mini_project.backend.services;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import ibf_tfip.mini_project.backend.models.Schedule;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

@Service
public class GoogleCalendarService {

    @Value("${google.calendar.api.key}")
    private String googleCalendarApiKey;

    @Autowired
    private GoogleTokenService googleTokenSvc;

    // https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
    private static final String timeZone = "Asia/Singapore";

    private String apiBaseUrl = "https://www.googleapis.com/calendar/v3/calendars/primary";


    // Create
    // https://developers.google.com/calendar/api/v3/reference/events/insert#http-request
    public String createSchedule(
        String summary, 
        String description, 
        String startDateTimeStr, 
        String endDateTimeStr, 
        String rruleStr,
        String email) throws IOException, GeneralSecurityException {

        String accessToken = googleTokenSvc.getAccessTokenFrRedis();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        // Creating the event JSON object
        JsonObjectBuilder eventBuilder = Json.createObjectBuilder()
            .add("summary", summary)
            .add("description", description)
            .add("start", Json.createObjectBuilder()
                .add("dateTime", startDateTimeStr)
                .add("timeZone", timeZone))
            .add("end", Json.createObjectBuilder()
                .add("dateTime", endDateTimeStr)
                .add("timeZone", timeZone))
            .add("recurrence", Json.createArrayBuilder()
                .add(rruleStr))
            .add("attendees", Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("email", email)))
            .add("reminders", Json.createObjectBuilder()
                .add("useDefault", false)
                .add("overrides", Json.createArrayBuilder()
                    .add(Json.createObjectBuilder()
                        .add("method", "email")
                        .add("minutes", 24 * 60))
                    .add(Json.createObjectBuilder()
                        .add("method", "popup")
                        .add("minutes", 10))));

        JsonObject eventJson = eventBuilder.build();

        try {
            String requestBody = eventJson.toString();
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            String apiUrl = apiBaseUrl + "/events?key=" + googleCalendarApiKey;

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                apiUrl,
                requestEntity,
                String.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                JsonObject response = Json.createReader(new StringReader(responseEntity.getBody())).readObject();
                return response.getString("htmlLink");
            } else {
                throw new RuntimeException("Failed to create schedule. Status code: " + responseEntity.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create schedule", e);
        }
    }


    // Read All
    // https://developers.google.com/calendar/api/v3/reference/events/list#http-request
    public Optional<List<Schedule>> getAllSchedules() throws IOException, GeneralSecurityException {

        String accessToken = googleTokenSvc.getAccessTokenFrRedis();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        
        List<Schedule> scheduleList = new ArrayList<>();

        try {
            String url = apiBaseUrl + "/events?key=" + googleCalendarApiKey;
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                requestEntity, 
                String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                JsonReader jsonReader = Json.createReader(new StringReader(responseEntity.getBody()));
                JsonObject jsonObject = jsonReader.readObject();

                for (JsonObject item : jsonObject
                    .getJsonArray("items")
                    .getValuesAs(JsonObject.class)) {
                        
                    Event event = convertItemToEvent(item);
                    scheduleList.add(convertEventToSchedule(event));
                }
            } else {
                throw new RuntimeException("Failed to get events. Status code: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

        return Optional.of(scheduleList);
    }


    // Delete
    // https://developers.google.com/calendar/api/v3/reference/events/delete#http-request
    public boolean deleteSchedule(String eventId) throws IOException, GeneralSecurityException {
        boolean isDeleted = false;
    
        String accessToken = googleTokenSvc.getAccessTokenFrRedis();
    
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
    
        try {
            String url = apiBaseUrl + "/events/" + eventId + "?key=" + googleCalendarApiKey;
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
    
            ResponseEntity<Void> responseEntity = restTemplate.exchange(
                url, 
                HttpMethod.DELETE, 
                requestEntity, 
                Void.class);
    
            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                isDeleted = true;
            } else {
                throw new RuntimeException("Failed to delete event. Status code: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return isDeleted;
    }


    //// HELPER METHODS ////////////////////////////////////////////////////////////////////
    private static LocalDateTime convertToLocalDateTime(EventDateTime eventDateTime) {
        if (eventDateTime == null || eventDateTime.getDateTime() == null) {
            return null; // Return null or handle as needed
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(eventDateTime.getDateTime().toStringRfc3339());
        return zonedDateTime.toLocalDateTime();
    }

    private static Schedule convertEventToSchedule(Event event) {
        Schedule schedule = new Schedule(
            event.getId(), 
            event.getSummary(), 
            event.getDescription(), 
            convertToLocalDateTime(event.getStart()),
            convertToLocalDateTime(event.getEnd()),
            event.getHtmlLink());
        return schedule;
    }

    private Event convertItemToEvent(JsonObject jsonObject) {
        Event event = new Event();
        event.setId(jsonObject.getString("id"));
        event.setSummary(jsonObject.getString("summary", null));
        event.setDescription(jsonObject.getString("description", null));
        event.setStart(convertToEventDateTime(jsonObject.getJsonObject("start")));
        event.setEnd(convertToEventDateTime(jsonObject.getJsonObject("end")));
        event.setHtmlLink(jsonObject.getString("htmlLink", null));
        // System.out.println(event.toString());
        return event;
    }

    private EventDateTime convertToEventDateTime(JsonObject jsonObject) {
        // System.out.println(jsonObject.toString());
        if (jsonObject == null || !jsonObject.containsKey("dateTime")) {
            return null; // or handle the missing dateTime case as needed
        }
    
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDateTime(new DateTime(jsonObject.getString("dateTime")));
    
        if (jsonObject.containsKey("timeZone")) {
            eventDateTime.setTimeZone(jsonObject.getString("timeZone"));
        }
    
        return eventDateTime;
    }
}
