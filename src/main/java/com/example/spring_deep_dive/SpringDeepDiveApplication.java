package com.example.spring_deep_dive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SpringDeepDiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDeepDiveApplication.class, args);
	}

}
