package com.ptit.projectmanagementbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Nếu không cần credentials, có thể sử dụng wildcard đơn giản
        config.setAllowCredentials(false); // hoặc bỏ dòng này
        config.addAllowedOrigin("*");

        // Cho phép tất cả các headers
        config.addAllowedHeader("*");

        // Cho phép tất cả các HTTP methods
        config.addAllowedMethod("*"); // Đơn giản hóa bằng cách cho phép tất cả methods

        // Thời gian cache lưu trữ các kết quả preflight request (giây)
        config.setMaxAge(3600L);

        // Áp dụng cấu hình này cho tất cả các đường dẫn API
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
