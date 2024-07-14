package ibf_tfip.mini_project.backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.server.ResponseStatusException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseAuth firebaseAuth;

    public SecurityConfig(FirebaseApp firebaseApp) {
        this.firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests -> 
            authorizeRequests
                .requestMatchers(
                    "/",
                    "/api/auth/signup",
                    "/api/auth/signin",
                    "/api/google/callback",
                    "/error"
                )
                .permitAll() // Allow unauthenticated access to signup
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable()) // Disable CSRF protection if not needed
            .addFilterBefore(
                new FirebaseAuthFilter(firebaseAuth), 
                UsernamePasswordAuthenticationFilter.class) // Add custom filter
            .formLogin(login -> login.disable()) // Disable default login form by Spring Security
            .httpBasic(basic -> basic.disable()); // Disable basic auth
        return http.build();
    }

    
    public String getAuthenticatedUserId() throws ResponseStatusException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof String)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authenticated or token is invalid");
        }
        // Depends on how you set in FirebaseAuthentication
        return (String) authentication.getPrincipal(); 
    }
}
