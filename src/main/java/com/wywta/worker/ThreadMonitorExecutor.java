/*
 * ThreadMonitorExecutor.java 2024-01-16
 *
 * @author joonyeong.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ThreadMonitorExecutor {
    private static final String LOG_PREFIX = "[THREAD-MONITOR]";
    private final ScheduledExecutorService executorService;

    private ScheduledFuture<?> scheduledFuture;

    public ThreadMonitorExecutor() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void startMonitoring() {
        scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            log.info("======================================");
            log.info("Thread ID | Thread Name | Thread State");
            log.info("======================================");

            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);

            for (ThreadInfo threadInfo : threadInfos) {
                long threadId = threadInfo.getThreadId();
                String threadName = threadInfo.getThreadName();
                if (threadName.contains("Executor") || threadName.contains("pool-")) {
                    Thread.State threadState = threadInfo.getThreadState();
                    log.info(String.format("%-10d | %-12s | %-15s%n", threadId, threadName, threadState));
                }
            }

            log.info("======================================");
        }, 0, 1, TimeUnit.SECONDS); // 매 5초마다 스레드 정보 출력
    }

    public void stopMonitoring() {

        if (Objects.nonNull(scheduledFuture)) {
            scheduledFuture.cancel(true);
            executorService.shutdown();
        }

        log.info("{} 종료합니다.", LOG_PREFIX);
    }

}
