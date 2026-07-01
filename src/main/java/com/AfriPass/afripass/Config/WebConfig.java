package com.AfriPass.afripass.Config;

import com.AfriPass.afripass.Interceptor.LoginRateLimitInterceptor;
import com.AfriPass.afripass.Interceptor.RateLimitInterceptor;
import org.hibernate.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor interceptor;
    @Autowired
    private LoginRateLimitInterceptor loginRateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){

        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/bookings/**" , "/api/payments/**");
        registry.addInterceptor(loginRateLimitInterceptor)
                .addPathPatterns("/api/auth/login");

    }

}
