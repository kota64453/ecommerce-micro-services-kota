package com.ecommerce.auth.redis;

import com.ecommerce.auth.dto.SignupRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupRedisService {

    private final StringRedisTemplate redisTemplate;
    private static final String OTP_PREFIX = "OTP:";
    private static final String SIGNUP_PREFIX = "SIGNUP:";
    private static final long OTP_TTL_MINUTES = 5;
    private static final long SIGNUP_TTL_MINUTES = 10;
    private final ObjectMapper objectMapper;
    public void storeOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("OTP stored for email: {} with TTL: {} minutes", email, OTP_TTL_MINUTES);
    }

    public void storeSignupRequest(SignupRequest request) {
        try {
            String key = SIGNUP_PREFIX + request.getEmail();
            String json = objectMapper.writeValueAsString(request);

            redisTemplate.opsForValue().set(
                    key,
                    json,
                    SIGNUP_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize signup request", e);
        }
    }

    public SignupRequest getSignupRequest(String email) {
        try {
            String key = SIGNUP_PREFIX + email;

            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                return null;
            }

            return objectMapper.readValue(json, SignupRequest.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize signup request", e);
        }
    }

    public String getOtp(String email) {
        String key = OTP_PREFIX + email;
        return redisTemplate.opsForValue().get(key);
    }

    public boolean validateOtp(String email, String otp) {
        String storedOtp = getOtp(email);
        if (storedOtp == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }
        boolean isValid = storedOtp.equals(otp);
        if (isValid) {
            deleteOtp(email);
            log.info("OTP validated successfully for email: {}", email);
        } else {
            log.warn("Invalid OTP provided for email: {}", email);
        }
        return isValid;
    }

    public void deleteOtp(String email) {
        String key = OTP_PREFIX + email;
        redisTemplate.delete(key);
        log.info("OTP deleted for email: {}", email);
    }

    public void deleteSignupData(String email) {

        String key = SIGNUP_PREFIX + email;

        Boolean deleted = redisTemplate.delete(key);

        if (deleted) {
            log.info("Signup data removed for email: {}", email);
        } else {
            log.warn("No signup data found for email: {}", email);
        }
    }


}
