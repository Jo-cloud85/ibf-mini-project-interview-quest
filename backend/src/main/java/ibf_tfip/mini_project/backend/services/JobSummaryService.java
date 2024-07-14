package ibf_tfip.mini_project.backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ibf_tfip.mini_project.backend.models.JobSummary;
import ibf_tfip.mini_project.backend.repositories.JobSummaryRepo;

@Service
public class JobSummaryService {

    @Autowired
    private JobSummaryRepo jobSummaryRepo;

    // Create
    public boolean addNewJobSummary(JobSummary jobSumm) {
        return jobSummaryRepo.addNewJobSummary(jobSumm);
    }

    // Read All - where query is optional
    public List<JobSummary> getAllJobSummaryByUserIdByQuery(String userId, String q) {
        return jobSummaryRepo.getAllJobSummaryByUserIdByQuery(userId, q);
    }

    // Read All - where isAttempted = 1
    public List<JobSummary> getAllAttemptedJobSummaryByUserId(String userId) {
        return jobSummaryRepo.getAllAttemptedJobSummaryByUserId(userId);
    }

    // Read
    public JobSummary getJobSummaryByCustomJobId(String customJobId) {
        return jobSummaryRepo.getJobSummaryByCustomJobId(customJobId);
    }

    // Update
    public boolean updateJobSummaryByCustomJobId(String customJobId, boolean isAttempted) {
        return jobSummaryRepo.updateJobSummaryByCustomJobId(customJobId, isAttempted);
    }

    // Delete
    public boolean deleteJobSummaryByCustomJobId(JobSummary jobSumm) {
        return jobSummaryRepo.deleteJobSummaryByCustomJobId(jobSumm);
    }
}
