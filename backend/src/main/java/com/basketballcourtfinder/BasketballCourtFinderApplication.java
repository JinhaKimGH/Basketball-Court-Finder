package com.basketballcourtfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("com.basketballcourtfinder.repository")
@EntityScan("com.basketballcourtfinder.entity")
@SpringBootApplication
public class BasketballCourtFinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasketballCourtFinderApplication.class, args);
	}

}
