package com.AfriPass.afripass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AfriPassApplication {

	public static void main(String[] args) {
		SpringApplication.run(AfriPassApplication.class, args);
	}

}