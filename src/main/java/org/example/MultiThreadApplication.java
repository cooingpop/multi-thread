/*
 * MultiThreadApplication.java 2024-01-16
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MultiThreadApplication implements CommandLineRunner {


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MultiThreadApplication.class, args);
        // Stop command를 받았을 때 서버를 종료하는 훅 등록
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
    }

    @Override
    public void run(String... args) {
        log.info("start");
    }
}
