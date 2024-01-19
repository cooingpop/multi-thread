/*
 * MultiThreadApplication.java 2024-01-16
 *
 * @author joonyeong.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta;

import com.wywta.service.CollectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableScheduling
public class MultiThreadApplication implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CollectorService collectorService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MultiThreadApplication.class, args);
        // Stop command를 받았을 때 서버를 종료하는 훅 등록
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
    }

    @Override
    public void run(String... args) {
        // 애플리케이션 시작 시 실행할 작업들
        // 종료 훅 등록
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // 애플리케이션 종료 시 실행할 작업들
            cleanup();
            // 안전하게 애플리케이션 종료
            collectorService.shutdown();
            SpringApplication.exit(applicationContext, () -> 0);
        }));

        // CollectService의 execute 메서드 호출
        log.info("start");
        collectorService.execute();
    }

    private void cleanup() {
        // 리소스 해제 및 정리 작업 수행
        System.out.println("Cleaning up resources...");
    }
}

@Slf4j
@RestController
class ApplicationController {

    @GetMapping("/stop")
    public void stopApplication() {
        log.info("stop");
        System.exit(0);
    }

}