package com.AfriPass.afripass.Config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.stripe.Stripe;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init(){
        Stripe.apiKey = stripeSecretKey;
        System.out.println("Stripe key loaded: " + (stripeSecretKey != null && !stripeSecretKey.isEmpty()));
    }

}
