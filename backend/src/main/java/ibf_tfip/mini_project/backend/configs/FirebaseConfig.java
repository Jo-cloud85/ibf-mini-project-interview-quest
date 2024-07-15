package ibf_tfip.mini_project.backend.configs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.util.Base64;

@Configuration
public class FirebaseConfig {
    
    private static final String databaseUrl 
        = "https://interviewquest-b0b2e-default-rtdb.asia-southeast1.firebasedatabase.app";


    @Bean
    public FirebaseApp firebaseApp() throws IOException {

        // Code generated from (and then modified) - you can only download once - if lost, just generate new private key:
        // https://console.firebase.google.com/project/interviewquest-b0b2e/settings/serviceaccounts/adminsdk

        // Convert json file contents to Base64 in the terminal first - i.e. cat interviewquest-b0b2e-firebase-adminsdk.json | base64
        // Store the base64 inside .env file like any other api keys etc
        // Note: this is not the firebase config file but the firebase adminsdk file
        String serviceAccountBase64 = System.getenv("FIREBASE_ADMINSDK_BASE64");
        
        if (serviceAccountBase64 == null) {
            throw new IllegalStateException("Service account Base64 not set in environment variables.");
        }

        // Sanitize the base64 string to remove any unintended whitespace characters
        serviceAccountBase64 = serviceAccountBase64.replaceAll("\\s", "");

        byte[] decodedBytes = Base64.getDecoder().decode(serviceAccountBase64);

        // Check if the decoded bytes are correctly formatted JSON
        // System.out.println("Decoded JSON: " + new String(decodedBytes));

        InputStream serviceAccountStream = new ByteArrayInputStream(decodedBytes);
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .setDatabaseUrl(databaseUrl)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        return FirebaseApp.getInstance();
    }
}
