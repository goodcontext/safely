package com.safely;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SafelyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafelyApplication.class, args);
	}

}
