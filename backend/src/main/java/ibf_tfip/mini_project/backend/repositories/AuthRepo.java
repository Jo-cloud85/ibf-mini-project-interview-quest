package ibf_tfip.mini_project.backend.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import ibf_tfip.mini_project.backend.models.UserAuthDetails;

@Repository
public class AuthRepo {
    
    @Autowired
    private JdbcTemplate template;

    public boolean createNewUser(UserAuthDetails userDetails) {
        return template.update(
            Queries.SQL_CREATE_NEW_ACCOUNT, 
            userDetails.getUserId(),
            userDetails.getFirstName(),
            userDetails.getLastName(),  
            userDetails.getEmail(),
            userDetails.isEmailVerified(),
            userDetails.getPassword(), 
            userDetails.getProfilePicUrl(),
            userDetails.isDisabled(),
            userDetails.getLastPasswordChange()
        ) > 0;
    }

    public boolean checkEmailExists(String email) {
        SqlRowSet rs = template.queryForRowSet(Queries.SQL_GET_ACCOUNT_BY_EMAIL, email);
        if (rs.next())
            return true;
        return false;
    }
}
