/*
 * DynamicThreadPoolManager.java 2024-01-19
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 1. ThreadPool 크기 늘리기 (setCorePoolSize 및 setMaxPoolSize)
 *
 * 목적: 동시에 실행되는 스레드의 수를 결정합니다.
 * 장점: 작업 처리 속도를 높이고 동시에 처리되는 작업 수를 늘릴 수 있습니다.
 * 주의사항: 스레드가 많아질수록 시스템 자원 소비가 증가하므로, 무작정 늘리면 안 됩니다.
 *
 * 2. 대기열 용량 늘리기 (setQueueCapacity)
 *
 * 목적: 대기열이 꽉 찰 때 대기 중인 작업들을 저장하는 용량을 결정합니다.
 * 장점: 대기열이 작업들을 일시적으로 보유하므로, 스레드가 모두 사용 중이더라도 작업을 처리할 수 있습니다.
 * 주의사항: 대기열이 무한정으로 크게 설정되면 OutOfMemoryError 등이 발생할 수 있으므로 적절한 크기로 설정해야 합니다.
 *
 *
 * Thread 를 만드는 시점
 *
 * ThreadPoolTaskExecutor에 새로운 task 를 submit 할때
 * pool 에 사용가능한 쓰레드가 있더라도 corePoolSize 보다 적은 쓰레드가 있거나
 * maxPoolSize 보다 적은 쓰레드가 실행중이고 queueCapacity 에 의해 정의된 큐사이즈가 가득차면 새 쓰레드를 생성합니다.
 */
@Slf4j
@Service
public class DynamicThreadPoolManager {

    private static final String LOG_PREFIX = "[DynamicThreadPoolManager]";

    @Autowired
    private ApplicationContext applicationContext;

    private final List<String> threaPoolTaskExetutorBeanNameList = List.of("registerExecutor", "handlerExecutor", "checkerExecutor");

    private final int poolSize = 10;

    private final int maximumPoolSize = 1000;

    private static ConcurrentMap<String, Integer> currentMaxActiveThread = new ConcurrentHashMap<>(){{
        put("registerExecutor", 0);
        put("handlerExecutor", 0);
        put("checkerExecutor", 0);
        // 추가적으로 필요한 초기화 작업 수행
    }};

    // 일정 주기로 스레드 풀 상태 확인 및 동적으로 늘리기
    @Scheduled(fixedDelay = 60000)
    public void monitorAndAdjustThreadPool() {
        log.info("{} start monitoring", LOG_PREFIX);

        for (String executorBeanName : threaPoolTaskExetutorBeanNameList) {
            monitorThreadPoolSize(executorBeanName);
        }
    }

    public void monitorThreadPoolSize(String executorBeanName) {
        log.info("{} Monitor target thread: {}", LOG_PREFIX, executorBeanName);
        ThreadPoolTaskExecutor executor = applicationContext.getBean(executorBeanName, ThreadPoolTaskExecutor.class);
        if (executor != null) {
            ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();

            if (threadPoolExecutor != null) {
                int activeCount = threadPoolExecutor.getActiveCount();
                int currentPoolSize = threadPoolExecutor.getPoolSize();
                int maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
                log.info("{} Active Threads: {}, current pool Size: {}, Max Pool Size: {}", LOG_PREFIX, activeCount, currentPoolSize, maxPoolSize);
                log.info("{} Active Top Threads: {}", LOG_PREFIX, currentMaxActiveThread);

                if (activeCount >= currentMaxActiveThread.get(executorBeanName)) {
                    currentMaxActiveThread.put(executorBeanName, activeCount);
                }

                // 활성화된 쓰레드가 20% 줄어들때 풀사이즈 조절
                if (activeCount >= currentMaxActiveThread.get(executorBeanName) * 0.8) {
                    decreaseMaxPoolSize(executor);
                    currentMaxActiveThread.put(executorBeanName, (int) Math.round(currentMaxActiveThread.get(executorBeanName) * 0.8));
                }

                if (activeCount == maxPoolSize) {
                    increaseMaxPoolSize(executor);
                }
            } else {
                log.warn("{} ThreadPoolExecutor is null for bean {}", LOG_PREFIX, executorBeanName);
            }
        } else {
            log.warn("{} Executor with bean name {} not found.", LOG_PREFIX, executorBeanName);
        }
    }

    private void increaseMaxPoolSize(ThreadPoolTaskExecutor executor) {
        if (maximumPoolSize <= executor.getMaxPoolSize()) {
            log.info("{} {} failed increase max pool size maximum Pool Size: {}", LOG_PREFIX, executor.getThreadNamePrefix(), maximumPoolSize);
            return;
        }
        int newMaxPoolSize = executor.getMaxPoolSize() + poolSize;
        executor.setMaxPoolSize(newMaxPoolSize);
        log.info("{} {} Increased Max Pool Size to: {}", LOG_PREFIX, executor.getThreadNamePrefix(), newMaxPoolSize);
    }

    private void decreaseMaxPoolSize(ThreadPoolTaskExecutor executor) {
        if (executor.getMaxPoolSize() >= poolSize) {
            return;
        }

        int newMaxPoolSize = executor.getMaxPoolSize() - poolSize;
        executor.setMaxPoolSize(newMaxPoolSize);
        log.info("{} {} Increased Max Pool Size to: {}", LOG_PREFIX, executor.getThreadNamePrefix(), newMaxPoolSize);
    }
}
