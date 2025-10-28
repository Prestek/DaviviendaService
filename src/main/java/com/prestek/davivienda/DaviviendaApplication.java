package com.prestek.davivienda;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DaviviendaApplication {

	private static final Logger logger = LoggerFactory.getLogger(DaviviendaApplication.class);

	public static void main(String[] args) {
		// Load environment variables from .env file only if not already set
		try {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			dotenv.entries().forEach(entry -> {
				String key = entry.getKey();

				// Only set the system property if it is not already set
				logger.info("Setting system property: {}={}", key, entry.getValue());
				System.setProperty(key, entry.getValue());
			});
		} catch (Exception e) {
			// Continue without .env file
		}
		SpringApplication.run(DaviviendaApplication.class, args);
	}
}
