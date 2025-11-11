package com.example.backend.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Service
public class OneTimeCodeService {

    private final Cache<String, String> codeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    /**
     * @param userEmail
     * @return 
     */
    public String generateAndStoreCode(String userEmail) {
        String code = UUID.randomUUID().toString();
        codeCache.put(code, userEmail);
        return code;
    }

    /**
     * @param code
     * @return 
     */
    public String getEmailForCode(String code) {
        String email = codeCache.getIfPresent(code);
        if (email != null) {
            codeCache.invalidate(code);
        }
        return email;
    }
}