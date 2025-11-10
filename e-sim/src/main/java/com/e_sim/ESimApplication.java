package com.e_sim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ESimApplication {

	public static void main(String[] args) {
		SpringApplication.run(ESimApplication.class, args);
	}

}
