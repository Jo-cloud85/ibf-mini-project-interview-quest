package ibf_tfip.mini_project.backend.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ibf_tfip.mini_project.backend.models.JobSummary;
import ibf_tfip.mini_project.backend.services.JobSummaryService;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/* For displaying data on dashboard overview and archive */

@RestController
@RequestMapping("/api/jobs")
public class JobSummaryController {

    // For getting/updating/deleting job summary from MySQL
    // The creating/saving part is in OpenAIAssistantController

    @Autowired
    private JobSummaryService jobSummarySvc;


    // Read All - where query is optional
    @GetMapping("/all-custom-jobs")
    @ResponseBody
    public ResponseEntity<String> getAllJobSummaryByUserIdByQuery(
        @RequestParam(required = false) String q) {
        
        try {
            String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

            List<JobSummary> allJobsByQuery = jobSummarySvc.getAllJobSummaryByUserIdByQuery(userId, q);

            if (allJobsByQuery.size() == 0) {
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body("No jobs found");
            }
    
            JsonArrayBuilder jsonArr = Json.createArrayBuilder();
            for (JobSummary job : allJobsByQuery) {
                JsonObjectBuilder jsonObjBuilder = job.convertToJson(job);
                jsonArr.add(jsonObjBuilder);
            }
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(jsonArr.build().toString());   
        } catch (Exception e)  {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + e.getMessage());
        }
    }


    // Read All - where isAttempted = 1
    @GetMapping("/all-custom-jobs-attempted")
    @ResponseBody
    public ResponseEntity<String> getAllAttemptedJobSummaryByUserId() {
        
        try {
            String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

            List<JobSummary> allJobsByQuery = jobSummarySvc.getAllAttemptedJobSummaryByUserId(userId);

            if (allJobsByQuery.size() == 0) {
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body("No jobs found");
            }
    
            JsonArrayBuilder jsonArr = Json.createArrayBuilder();
            for (JobSummary job : allJobsByQuery) {
                JsonObjectBuilder jsonObjBuilder = job.convertToJson(job);
                jsonArr.add(jsonObjBuilder);
            }
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(jsonArr.build().toString());   
        } catch (Exception e)  {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + e.getMessage());
        }
    }
        

    // Read
    @GetMapping("/custom-job/{customJobId}")
    public ResponseEntity<String> getJobSummaryByCustomJobId(
        @PathVariable String customJobId) {

        try {
            JobSummary jobSummary = jobSummarySvc.getJobSummaryByCustomJobId(customJobId);
            JsonObject jsonObj = jobSummary.convertToJson(jobSummary).build();
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(jsonObj.toString());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + e.getMessage());
        }
    }


    // Update
    @PutMapping("/custom-job/{customJobId}")
    public ResponseEntity<String> updateJobSummaryByCustomJobId(
        @PathVariable String customJobId,
        @RequestBody Map<String, Boolean> requestBody) {

        boolean isAttempted = requestBody.get("isAttempted");
        
        try {
            Boolean isUpdated = jobSummarySvc.updateJobSummaryByCustomJobId(customJobId, isAttempted);
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(isUpdated.toString());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
