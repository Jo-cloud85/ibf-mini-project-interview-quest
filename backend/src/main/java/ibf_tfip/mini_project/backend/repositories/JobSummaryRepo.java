package ibf_tfip.mini_project.backend.repositories;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import ibf_tfip.mini_project.backend.models.JobSummary;

@Repository
public class JobSummaryRepo implements Queries {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Create
    public boolean addNewJobSummary(JobSummary jobSumm) {
        int isAdded = 0;
        isAdded = jdbcTemplate.update(
            SQL_CREATE_NEW_CUSTOM_JOB_FOR_USER,
            jobSumm.getUserId(),
            jobSumm.getCustomJobId(),
            jobSumm.getAssistantId(),
            jobSumm.getThreadId(),
            jobSumm.getFirebaseThreadKey(),
            jobSumm.getTitle(),
            jobSumm.getLevel(),
            jobSumm.getCreatedTime(),
            jobSumm.isAttempted()
        );
        return isAdded > 0 ? true : false;
    }


    // Read All - where query is optional
    public List<JobSummary> getAllJobSummaryByUserIdByQuery(String userId, String q) {
        String queryStr = "%" + q + "%";

        SqlRowSet rs = jdbcTemplate.queryForRowSet(
            SQL_GET_ALL_CUSTOM_JOBS_BY_USER_ID_BY_QUERY, userId, queryStr, queryStr);

        List<JobSummary> jobSummList = new LinkedList<>();
        while (rs.next()) {
            JobSummary jobSumm = setJobSummary(rs);
            jobSummList.add(jobSumm);
        } 
        return jobSummList;
    }

    // Read All - where isAttempted = 1
    public List<JobSummary> getAllAttemptedJobSummaryByUserId(String userId) {
        SqlRowSet rs = jdbcTemplate.queryForRowSet(
            SQL_GET_ALL_ATTEMPTED_CUSTOM_JOBS_BY_USER_ID, userId);

        List<JobSummary> jobSummList = new LinkedList<>();
        while (rs.next()) {
            JobSummary jobSumm = setJobSummary(rs);
            jobSummList.add(jobSumm);
        } 
        return jobSummList;
    }


    // Read
    public JobSummary getJobSummaryByCustomJobId(String customJobId) {
        SqlRowSet rs = jdbcTemplate.queryForRowSet(
            SQL_GET_CUSTOM_JOB_BY_CUSTOM_JOB_ID, customJobId);

        if (rs.next()) {
            JobSummary jobSumm = setJobSummary(rs);
            return jobSumm;
        } else {
            return null;
        }
    }


    // Update
    public boolean updateJobSummaryByCustomJobId(String customJobId, boolean isAttempted) {
        int isUpdated = jdbcTemplate.update(
            SQL_UPDATE_CUSTOM_JOB_BY_CUSTOM_JOB_ID, isAttempted, customJobId);

        return isUpdated > 0 ? true : false;
    }
    

    // Delete
    public boolean deleteJobSummaryByCustomJobId(JobSummary jobSumm) {
        int isDeleted = jdbcTemplate.update(
            SQL_DELETE_CUSTOM_JOB_BY_CUSTOM_JOB_ID, jobSumm.getCustomJobId());
            
        return isDeleted > 0 ? true : false;
    }

    
    // Helper for Read
    private JobSummary setJobSummary(SqlRowSet rs) {
        JobSummary jobSumm = new JobSummary();
        jobSumm.setUserId(rs.getString("userId"));
        jobSumm.setCustomJobId(rs.getString("customJobId"));
        jobSumm.setAssistantId(rs.getString("assistantId"));
        jobSumm.setThreadId(rs.getString("threadId"));
        jobSumm.setFirebaseThreadKey(rs.getString("firebaseThreadKey"));
        jobSumm.setTitle(rs.getString("title"));
        jobSumm.setLevel(rs.getString("level"));
        LocalDateTime createdTime = (LocalDateTime) rs.getTimestamp("createdTime")
            .toInstant()
            .atZone(ZoneId.of("UTC"))
            .toLocalDateTime();
        jobSumm.setCreatedTime(createdTime);
        jobSumm.setAttempted(rs.getBoolean("isAttempted"));
        return jobSumm;
    }
}
