package ibf_tfip.mini_project.backend.repositories;

public interface Queries {
    
    // Auth - registering new account (aka user)
    public static final String SQL_CREATE_NEW_ACCOUNT = """
            INSERT into user_auth_details
                (userId, firstName, lastName, email, isEmailVerified, 
                hashedPassword, profilePicUrl, isDisabled, lastPasswordChange)
            VALUES 
                (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    // Auth - login - finding account/user ----------------------------------------
    public static final String SQL_GET_ACCOUNT_BY_EMAIL = """
            SELECT 1 
            FROM user_auth_details
            WHERE email = ?
        """;
    
    // Auth - if successful - getting details to show on profile -------------------
    public static final String SQL_GET_ACCOUNT_BY_USER_ID = """
            SELECT userId, firstName, lastName, email, profilePicUrl
            FROM user_auth_details
            WHERE userId = ?
        """;


    // Custom job --------------------------------------------------
    // Create
    public static final String SQL_CREATE_NEW_CUSTOM_JOB_FOR_USER = """
            INSERT into job_summary
                (userId, customJobId, assistantId, threadId, firebaseThreadKey, 
                title, level, createdTime, isAttempted)
            VALUES
                (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    
    // Read All - where query is optional
    public static final String SQL_GET_ALL_CUSTOM_JOBS_BY_USER_ID_BY_QUERY = """
            SELECT *
            FROM job_summary   
            WHERE userId = ? 
            AND (title LIKE ? OR level LIKE ?)    
        """;
    
    // Read All - where isAttempted = 1
    public static final String SQL_GET_ALL_ATTEMPTED_CUSTOM_JOBS_BY_USER_ID = """
            SELECT *
            FROM job_summary   
            WHERE userId = ? 
            AND isAttempted = 1
        """;

    // Read
    public static final String SQL_GET_CUSTOM_JOB_BY_CUSTOM_JOB_ID = """
            SELECT *
            FROM job_summary 
            WHERE customJobId = ?
        """;

    // Update
    public static final String SQL_UPDATE_CUSTOM_JOB_BY_CUSTOM_JOB_ID = """
            UPDATE job_summary
            SET isAttempted = ?
            WHERE customJobId = ?
        """;

    // Delete
    public static final String SQL_DELETE_CUSTOM_JOB_BY_CUSTOM_JOB_ID = """
            DELETE FROM job_summary
            WHERE customJobId = ?
        """;
}
