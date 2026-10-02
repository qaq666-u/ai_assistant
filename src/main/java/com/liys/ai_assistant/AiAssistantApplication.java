package com.liys.ai_assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main application class for the AI Assistant application.
 * This class is annotated with @SpringBootApplication, which is a convenience annotation
 * that combines @Configuration, @EnableAutoConfiguration, and @ComponentScan.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AiAssistantApplication {

/**
 * Main method to run the Spring Boot application.
 * This is the entry point of the application when it starts.
 *
 * @param args Command line arguments passed to the application
 */
    public static void main(String[] args) {
    // Use Spring Boot's SpringApplication.run method to start the application
    // The first argument is the main application class, and the second argument
    // contains the command line arguments passed to the application
        SpringApplication.run(AiAssistantApplication.class, args);
    }

}
