package com.walt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WaltApplication {

    private static final Logger log = LoggerFactory.getLogger(WaltApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WaltApplication.class);
    }

}
