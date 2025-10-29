package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		try {
            Dotenv dotenv = Dotenv.load();
            dotenv.entries().forEach(entry -> 
                System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            System.err.println("Cảnh báo: Không tìm thấy file .env. Tiếp tục với các biến môi trường/properties đã có.");
        }
		SpringApplication.run(BackendApplication.class, args);
	}

}
