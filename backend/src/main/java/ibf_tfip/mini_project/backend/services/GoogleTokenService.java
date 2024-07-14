package ibf_tfip.mini_project.backend.services;

import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import ibf_tfip.mini_project.backend.utils.GeneralUtils;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class GoogleTokenService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Autowired
    @Qualifier(GeneralUtils.REDIS_BEAN_1)
    private RedisTemplate<String, String> redisTemplateForToken;


    public void saveTokenJsonStrInRedis(String responseBody) {
        redisTemplateForToken.opsForValue().set(GeneralUtils.KEY_TOKENS, responseBody);
    }

    
    public String getAccessTokenFrRedis() {
        String existingTokensJsonStr = redisTemplateForToken.opsForValue().get(GeneralUtils.KEY_TOKENS);

        if (isTokenExpired(existingTokensJsonStr)) {
            return extractTokenData("access_token", refreshAccessToken());
        } else {
            return extractTokenData("access_token", existingTokensJsonStr);
        }
    }

    // In case access token expires, for offline access
    // https://developers.google.com/identity/protocols/oauth2/web-server#offline
    public String refreshAccessToken() {
        // Old tokensJsonStr
        String tokensJsonStr = redisTemplateForToken.opsForValue().get(GeneralUtils.KEY_TOKENS);

        // Setting up to get new access token with the refresh token
        String tokenUrl = "https://oauth2.googleapis.com/token";
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", extractTokenData("refresh_token", tokensJsonStr));
        params.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, requestEntity, String.class);
        String newTokensJsonStr = response.getBody();

        if (response.getStatusCode() == HttpStatus.OK && newTokensJsonStr != null) {
            redisTemplateForToken.opsForValue().set(GeneralUtils.KEY_TOKENS, newTokensJsonStr);
            return newTokensJsonStr;
        } else {
            throw new RuntimeException("Failed to refresh access token");
        }
    }
    

    // Check for token expiry
    public boolean isTokenExpired(String tokensJsonStr) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(tokensJsonStr))) {
            JsonObject jsonObject = jsonReader.readObject();
            int expiresIn = jsonObject.getInt("expires_in");
            long expirationTimestamp = System.currentTimeMillis() + (expiresIn * 1000L);
            return System.currentTimeMillis() > expirationTimestamp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    // Extract token
    private String extractTokenData(String tokenType, String tokensJsonStr) {
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(tokensJsonStr));
            JsonObject jsonObject = jsonReader.readObject();
            return jsonObject.getString(tokenType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
