package ibf_tfip.mini_project.backend.controllers;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import ibf_tfip.mini_project.backend.services.GoogleTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

// For redirecting users through the OAuth 2.0 flow to get their consent and obtain the 
// necessary tokens to access their Google Calendar data

@RestController
@RequestMapping("/api/google")
public class GoogleAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Autowired
    private GoogleTokenService googleTokenSvc;

    private String baseUrl = "http://localhost:8080";


    // https://developers.google.com/identity/protocols/oauth2/web-server#sample-oauth-2.0-server-response
    @GetMapping("/authorize-url")
    public ResponseEntity<Map<String, String>> authorize() {

        String authorizationUrl = "https://accounts.google.com/o/oauth2/auth" +
                "?scope=https://www.googleapis.com/auth/calendar" +
                "&access_type=offline" +
                "&include_granted_scopes=true" +
                "&response_type=code" +
                "&state=helloworld" +
                "&redirect_uri=" + redirectUri +
                "&client_id=" + clientId;

        Map<String, String> response = new HashMap<>();
        response.put("url", authorizationUrl);

        // System.out.println("Authorization url: " + authorizationUrl);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Automatically redirect to this endpoint by Google once user is authorized
    // This endpoint exchanges the authorization code for tokens and then redirects to the frontend application.
    // https://developers.google.com/identity/protocols/oauth2/web-server#exchange-authorization-code
    @GetMapping("/callback") 
    public ResponseEntity<Void> handleCallback(@RequestParam("code") String code) {

        // Exchange code for tokens
        String tokenUrl = "https://oauth2.googleapis.com/token";

        RestTemplate restTemplate = new RestTemplate();

        // Setting up the code and credentials to exchange for access token and refresh token 
        // - you need this when u manage schedules with the Google Calendar
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, requestEntity, String.class);
        
        // This is the json object string with access_token, refresh_token etc.
        String tokenJsonStr = response.getBody(); 

        // Store the json object in Redis
        if (response.getStatusCode() == HttpStatus.OK && tokenJsonStr != null) {
            try {
                googleTokenSvc.saveTokenJsonStrInRedis(tokenJsonStr);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // Redirect to frontend with a success flag
            HttpHeaders redirectHeaders = new HttpHeaders();
            redirectHeaders.setLocation(URI.create(baseUrl + "/interview-quest/schedule?success=true"));
            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
