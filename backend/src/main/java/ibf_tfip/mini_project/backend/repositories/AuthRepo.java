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

    // public String getHashedPasswordByEmail(String email) {
    //     SqlRowSet rs = template.queryForRowSet(Queries.SQL_AUTH_GET_HASHED_PASSWORD_BY_EMAIL, email);
    //     if (rs.next())
    //         return rs.getString("passwordHash");
    //     return null;
    // }

    // public String getHashedPasswordById(String userId) {
    //     SqlRowSet rs = template.queryForRowSet(Queries.SQL_AUTH_GET_HASHED_PASSWORD_BY_ID, userId);
    //     if (rs.next())
    //         return rs.getString("passwordHash");
    //     return null;
    // }

    // public String updateUserPassword(String userId, String newHashedPassword) {
    //    int rowsAffected = template.update(Queries.SQL_AUTH_PASSWORD_CHANGE, newHashedPassword, userId);
    //    if (rowsAffected==0) return null;
    //    return newHashedPassword;
    // }
}
