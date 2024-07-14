package ibf_tfip.mini_project.backend.controllers;

import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

import ibf_tfip.mini_project.backend.models.UserAuthDetails;
import ibf_tfip.mini_project.backend.services.FirebaseAuthService;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

// For sign-in and sign-ups

@RestController
@RequestMapping("/api/auth")
public class FirebaseAuthController {
    
    @Autowired
    private FirebaseAuthService fbAuthSvc;

    // Sign Up
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody String requestPayload) {

        JsonReader jsonReader = Json.createReader(new StringReader(requestPayload));
        JsonObject requestJsonObj = jsonReader.readObject();

        String email = requestJsonObj.getString("email");
        String password = requestJsonObj.getString("password");
        String firstName = requestJsonObj.getString("firstName");
        String lastName = requestJsonObj.getString("lastName");

        if (fbAuthSvc.isEmailTaken(email)) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Email is already taken.");
        }

        try {
            UserAuthDetails user = new UserAuthDetails(
                firstName, 
                lastName,
                email, 
                false, 
                password, 
                "null", 
                false, 
                null);

            UserRecord newUser = fbAuthSvc.createUser(user);
            String customToken = fbAuthSvc.generateCustomToken(newUser);

            JsonObject jsonObj = Json.createObjectBuilder()
                .add("custom_token", customToken)
                .build();
            return ResponseEntity.status(HttpStatus.OK).body(jsonObj.toString());
        } catch (FirebaseAuthException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating user: " + e.getMessage());
        }
    }


    // Sign In
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(
        @RequestBody String requestPayload) {

        // Retrieve the authenticated user's details from the SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof String)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("User is not authenticated or token is invalid");
        }

        String uid = (String) authentication.getPrincipal();

        try {
            JsonReader jsonReader = Json.createReader(new StringReader(requestPayload));
            JsonObject requestJsonObj = jsonReader.readObject();
            String email = requestJsonObj.getString("email");

            // Optionally, check if the email from the request matches the authenticated user's email
            FirebaseToken decodedToken = (FirebaseToken) authentication.getDetails();
            if (!decodedToken.getEmail().equals(email)) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Email does not match token");
            }

            // Generate a custom token for the user
            String customToken = FirebaseAuth.getInstance().createCustomToken(uid);

            JsonObject responseObj = Json.createObjectBuilder()
                .add("message", "Sign-in successful")
                .add("custom_token", customToken)
                .build();
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseObj.toString());
        } catch (FirebaseAuthException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating custom token: " + e.getMessage());
        }
    }

    // Read particulars
    @GetMapping("/profile")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        try {
            UserRecord user = fbAuthSvc.getUserByEmail(email);
            System.out.println(user);
            JsonObject jsonObj = Json.createObjectBuilder()
                .add("uid", user.getUid())
                .add("displayName", user.getDisplayName())
                .add("email", user.getEmail())
                .build();
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(jsonObj.toString());

        } catch (FirebaseAuthException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching user details: " + e.getMessage());
        }
    }


    // Update particulars
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody String requestPayload) {
        JsonReader jsonReader = Json.createReader(new StringReader(requestPayload));
        JsonObject requestJsonObj = jsonReader.readObject();

        String email = requestJsonObj.getString("email");
        String displayName = requestJsonObj.getString("displayName");
        String photoUrl = requestJsonObj.getString("photoUrl");

        try {
            // Fetch the user by email to get the UID
            UserRecord userRecord = fbAuthSvc.getUserByEmail(email);
            String uid = userRecord.getUid();

            // Split displayName into firstName and lastName for the sake of example
            // Assuming displayName is in "First Last" format
            String[] nameParts = displayName.split(" ", 2);
            String firstName = nameParts.length > 0 ? nameParts[0] : "";
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            // Create UserAuthDetails object
            UserAuthDetails userDetails = new UserAuthDetails(
                uid,
                firstName,
                lastName,
                email,
                userRecord.isEmailVerified(),
                null, // null since we are not updating it here
                photoUrl,
                userRecord.isDisabled(),
                null // null since we are not updating it here
            );

            // Update the user
            UserRecord updatedUser = fbAuthSvc.updateUserByUid(userDetails);
            JsonObject jsonObj = Json.createObjectBuilder()
                .add("displayName", updatedUser.getDisplayName())
                .add("email", updatedUser.getEmail())
                .add("photoUrl", updatedUser.getPhotoUrl())
                .build();
            return ResponseEntity.status(HttpStatus.OK).body(jsonObj.toString());
        } catch (FirebaseAuthException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating user details: " + e.getMessage());
        }
    }
}
