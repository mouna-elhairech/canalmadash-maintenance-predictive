package com.canal.canalmadash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CanalMaDashApplication {

    /**
     * Point d'entree du backend Spring Boot.
     * Cette methode demarre l'API REST utilisee par le dashboard web
     * et par l'application mobile.
     */
    public static void main(String[] args) {
        SpringApplication.run(CanalMaDashApplication.class, args);
    }

}
