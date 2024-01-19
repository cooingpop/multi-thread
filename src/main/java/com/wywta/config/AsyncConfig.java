/*
 * AsyncConfig.java 2024-01-16
 *
 * @author joonyeong.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // ThreadPool에서 항상 유지되는 최소 스레드수
    private final int corePoolSize = 2;

    // 최대 스레드 수
    private final  int maxPoolSize = 25;

    private final  int queueCapacity = 200;

    private final  int keepAliveSeconds = 60;

    @Bean(name = "registerExecutor")
    public ThreadPoolTaskExecutor asyncRegisterExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("RegisterExecutor-");
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.initialize();
        return executor;
    }

    @Bean(name = "handlerExecutor")
    public ThreadPoolTaskExecutor asyncHandlerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("HandlerExecutor-");
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.initialize();
        return executor;
    }

    @Bean(name = "checkerExecutor")
    public ThreadPoolTaskExecutor asyncCheckerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("CheckerExecutor-");
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.initialize();
        return executor;
    }
}
