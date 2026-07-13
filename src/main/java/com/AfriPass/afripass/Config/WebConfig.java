package com.AfriPass.afripass.Config;

import com.AfriPass.afripass.Interceptor.LoginRateLimitInterceptor;
import com.AfriPass.afripass.Interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final LoginRateLimitInterceptor loginRateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/bookings/**", "/api/payments/**");
        registry.addInterceptor(loginRateLimitInterceptor)
                .addPathPatterns("/api/auth/login");

    }

}
