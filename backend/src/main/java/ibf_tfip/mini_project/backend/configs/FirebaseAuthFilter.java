package ibf_tfip.mini_project.backend.configs;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain chain) throws IOException, ServletException {

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); 
            try {
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                FirebaseAuthentication authentication = new FirebaseAuthentication(decodedToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Authenticated user: " + decodedToken.getUid());
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                System.out.println("Authentication failed: " + e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
