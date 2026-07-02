package com.AfriPass.afripass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("com.AfriPass.afripass")
@EnableJpaRepositories("com.AfriPass.afripass.Repositories")
@EnableScheduling
public class AfriPassApplication {

	public static void main(String[] args) {
		SpringApplication.run(AfriPassApplication.class, args);
	}

}