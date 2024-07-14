package ibf_tfip.mini_project.backend.configs;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.google.firebase.auth.FirebaseToken;


public class FirebaseAuthentication implements Authentication {

    private final FirebaseToken firebaseToken;
    private boolean authenticated;

    public FirebaseAuthentication(FirebaseToken firebaseToken) {
        this.firebaseToken = firebaseToken;
        this.authenticated = true; // Mark as authenticated since token is valid
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // Implement if you have roles/authorities to return
    }

    @Override
    public Object getCredentials() {
        return firebaseToken.getEmail(); // Or other relevant credential
    }

    @Override
    public Object getDetails() {
        return firebaseToken; // Return details about the token
    }

    @Override
    public Object getPrincipal() {
        return firebaseToken.getUid(); // User ID from Firebase token
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return firebaseToken.getName(); // Or any other identifier
    }
}
