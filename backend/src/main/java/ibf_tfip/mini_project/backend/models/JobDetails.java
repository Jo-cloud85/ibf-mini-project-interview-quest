package ibf_tfip.mini_project.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is for creating message content for AI

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDetails {
    private String jobTitle;
    private String jobDescription;
    private String jobLevel;
    private String companyName;
    private String companyDetails;
}
