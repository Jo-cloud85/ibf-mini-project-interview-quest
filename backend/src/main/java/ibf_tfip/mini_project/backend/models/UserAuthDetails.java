package ibf_tfip.mini_project.backend.models;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthDetails {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isEmailVerified;
    private String password;
    private String profilePicUrl;
    private boolean isDisabled;
    private Date lastPasswordChange = null;

    // W/o uid
    public UserAuthDetails(
        String firstName, 
        String lastName, 
        String email, 
        boolean isEmailVerified,
        String password, 
        String profilePicUrl, 
        boolean isDisabled,
        Date lastPasswordChange) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isEmailVerified = isEmailVerified;
        this.password = password;
        this.profilePicUrl = profilePicUrl;
        this.isDisabled = isDisabled;
        this.lastPasswordChange = lastPasswordChange;
    }
}
