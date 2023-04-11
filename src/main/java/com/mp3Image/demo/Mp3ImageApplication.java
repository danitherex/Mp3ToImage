package com.mp3Image.demo;

import com.mp3Image.demo.mp3.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class Mp3ImageApplication {

	public static void main(String[] args) {
		SpringApplication.run(Mp3ImageApplication.class, args);
	}

}
