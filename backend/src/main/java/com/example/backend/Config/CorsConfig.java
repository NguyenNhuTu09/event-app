package com.example.backend.Config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", 
                                                "http://localhost:5173", 
                                                "https://event-management-system-drab-psi.vercel.app", 
                                                "https://webie-event-management-system.vercel.app", 
                                                "https://event-app-ten-flax.vercel.app",
                                                "https://ems-webie.vercel.app",
                                                "http://webie.io.vn",
                                                "https://webie.io.vn",
                                                "http://ems.webie.com.vn",
                                                "https://ems.webie.com.vn"
                                                ));
                                                
        configuration.setAllowedOriginPatterns(List.of("*")); // sau khi kết thúc giai đoạn test, nhớ xóa quyền truy cập này

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}