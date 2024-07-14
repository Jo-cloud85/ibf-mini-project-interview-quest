package ibf_tfip.mini_project.backend.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo extends UserAuthDetails {

    private List<JobSummary> customJobIds = new ArrayList<>();

    // Full constructor with lastPasswordChange
    public UserInfo(
        String userId, 
        String firstName, 
        String lastName, 
        String email, 
        boolean isEmailVerified,
        String hashPassword, 
        String profilePicUrl, 
        boolean isDisabled, 
        Date lastPasswordChange,
        List<JobSummary> customJobIds) {
            
        super(userId, firstName, lastName, email, isEmailVerified, hashPassword, profilePicUrl, isDisabled, lastPasswordChange);
        this.customJobIds = customJobIds;
    }
}

