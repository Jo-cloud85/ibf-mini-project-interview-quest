package ibf_tfip.mini_project.backend.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.UserRecord.UpdateRequest;

import ibf_tfip.mini_project.backend.models.UserAuthDetails;
import ibf_tfip.mini_project.backend.repositories.AuthRepo;

@Service
public class FirebaseAuthService {

    private final FirebaseAuth auth;

    @Autowired
    private AuthRepo authRepo;

    public FirebaseAuthService(FirebaseApp firebaseApp) {
        this.auth = FirebaseAuth.getInstance(firebaseApp);
    }

    //// Authentication ////////////////////////////////////////////////////////
    // To check email upon new sign ups
    public boolean isEmailTaken(String email) {
        try {
            UserRecord userRecord = auth.getUserByEmail(email);
            if (userRecord != null) {
                return true;
            }
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                return false;
            }
            throw new RuntimeException("Error checking email", e);
        }
        return false;
    }


    // Create custom token
    // https://firebase.google.com/docs/auth/admin/create-custom-tokens#create_custom_tokens_using_the_firebase_admin_sdk
    public String generateCustomToken(UserRecord user) {
        try {
            return auth.createCustomToken(user.getUid());
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error generating custom token", e);
        }
    }


    // Get User 
    // https://firebase.google.com/docs/auth/admin/manage-users#retrieve_user_data
    public UserRecord getUserByEmail(String email) throws FirebaseAuthException {

        UserRecord userRecord = auth.getUserByEmail(email);
        System.out.println("Successfully fetched user data: " + userRecord.getEmail());
        return userRecord;
    }


    // Get All Users 
    // https://firebase.google.com/docs/auth/admin/manage-users#list_all_users
    public List<UserRecord> getAllUsers() throws FirebaseAuthException {

        List<UserRecord> listOfUsers = new ArrayList<>();

        ListUsersPage page = auth.listUsers(null);
        while (page != null) {
            for (ExportedUserRecord user : page.getValues()) {
                System.out.println("User: " + user.getUid());
            }
            page = page.getNextPage();
        }

        page = auth.listUsers(null);
        for (ExportedUserRecord user : page.iterateAll()) {
            System.out.println("User: " + user.getUid());
            listOfUsers.add(user);
        }

        return listOfUsers;
    }


    // Create User
    // https://firebase.google.com/docs/auth/admin/manage-users#create_a_user
    public UserRecord createUser(
        UserAuthDetails userDetails) throws FirebaseAuthException {

        CreateRequest request = new CreateRequest()
            .setEmail(userDetails.getEmail())
            .setEmailVerified(userDetails.isEmailVerified())
            .setPassword(userDetails.getPassword()) // raw unhashed password
            .setDisplayName(userDetails.getFirstName() + " " + userDetails.getLastName())
            .setDisabled(false); // set to false by default
    
        // Create user in Firebase Authentication (where uid will be auto-generated)
        UserRecord userRecord = auth.createUser(request);

        String userId = userRecord.getUid();

        // Set the auto-generated uid back to userDetails
        userDetails.setUserId(userId);

        // Save userDetails to MySql
        authRepo.createNewUser(userDetails);

        System.out.println("Successfully created new user: " + userRecord.getUid());
        return userRecord;
    }


    // Update User
    // https://firebase.google.com/docs/auth/admin/manage-users#update_a_user
    public UserRecord updateUserByUid(
        UserAuthDetails userDetails) throws FirebaseAuthException {

        UpdateRequest request = new UpdateRequest(userDetails.getUserId())
            .setEmail(userDetails.getEmail())
            .setEmailVerified(userDetails.isEmailVerified())
            .setPassword(userDetails.getPassword())
            .setDisplayName((
                userDetails.getLastName() + userDetails.getFirstName()))
            .setDisabled(false); // set to false by default

        // Only set the photo URL if it is not null or empty
        if (userDetails.getProfilePicUrl() != null && !userDetails.getProfilePicUrl().isEmpty()) {
            request.setPhotoUrl(userDetails.getProfilePicUrl());
        }

        UserRecord userRecord = auth.updateUser(request);
        System.out.println("Successfully updated user: " + userRecord.getUid());
        return userRecord;
    }


    // Delete User
    // https://firebase.google.com/docs/auth/admin/manage-users#delete_a_user
    public boolean deleteUser(
        UserAuthDetails userInfo) throws FirebaseAuthException {

        boolean isDeleted = false;
        
        auth.deleteUser(userInfo.getUserId());
        System.out.println("Successfully deleted user.");
        isDeleted = true;
        return isDeleted;
    }
}       
