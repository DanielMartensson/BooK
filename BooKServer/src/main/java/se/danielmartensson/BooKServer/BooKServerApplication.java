package se.danielmartensson.BooKServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BooKServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooKServerApplication.class, args);
	}

}
