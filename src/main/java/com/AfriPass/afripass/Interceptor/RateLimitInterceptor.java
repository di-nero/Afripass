package com.AfriPass.afripass.Interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String , Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request , HttpServletResponse response , Object handler) throws Exception {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (userId == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Authentication required");
            return false;
        }

        Bucket bucket  = resolveBucket(userId);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()){

            log.info("Request ALLOWED for user={} , token remaining={}", userId , probe.getRemainingTokens());
            return true;

        } else {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;

            log.warn("Request REJECTED for user={} , retry after {}s", userId , waitSeconds);

            response.setStatus(429);
            response.setHeader("Retry-After" , String.valueOf(waitSeconds));
            response.getWriter().write("Too many requests. Try again in " + waitSeconds + " seconds.");
            return false;
        }
    }

    private Bucket resolveBucket(String userId){
        return buckets.computeIfAbsent(userId , id -> newBucket());
    }

    private Bucket newBucket(){

        Refill refill = Refill.greedy(5 , Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(5 , refill);

        return Bucket.builder().addLimit(limit).build();

    }

}