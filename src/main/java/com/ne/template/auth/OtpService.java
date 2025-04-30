package com.ne.template.auth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
@Slf4j
public class OtpService {
    private final RedisTemplate<String, String> redisTemplate;

    private final ValueOperations<String, String> valueOperations;

    String generateOtp(String userEmail, OtpType otpType){
        var otp = generateOtp();
        String key = generateKey(userEmail, otp, otpType);
        storeOtp(key, otp);
        return otp;
    }

    boolean verifyOtp(String userEmail, String otp, OtpType otpType){
        String key = generateKey(userEmail, otp, otpType);
        if (hasOtp(key)){
            String storedOtp = getOtp(key);
            if (storedOtp.equals(otp)){
                deleteOtp(key);
                return true;
            }
        }
        return false;
    }

    private String getOtp(String key){
        return valueOperations.get(key);
    }

    private void deleteOtp(String key){
        redisTemplate.delete(key);
    }

    private boolean hasOtp(String key){
        return redisTemplate.hasKey(key);
    }

    private String generateKey(String userEmail,String otp, OtpType otpType){
        return String.format("%s:%s:%s",otpType.toString(), userEmail, otp);
    }

    private void storeOtp(String key, String otp){
        valueOperations.set(key, otp, 10, TimeUnit.MINUTES);
        log.info("Storing otp is going successfully");
    }

    private String generateOtp(){
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = (int) (Math.random() * 10);
            otp.append(digit);
        }
        return otp.toString();
    }
}
