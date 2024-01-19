/*
 * CollectorService.java 2024-01-16
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.service;

import com.wywta.model.Constants;
import com.wywta.worker.ThreadMonitorExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CollectorService {
    private static final String LOG_PREFIX = "[CollectorService]";

    private final AsyncWorkerService asyncWorkerService;

    private final ThreadMonitorExecutor threadMonitorExecutor;

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    public CollectorService(AsyncWorkerService asyncWorkerService, ThreadMonitorExecutor threadMonitorExecutor) {
        this.asyncWorkerService = asyncWorkerService;
        this.threadMonitorExecutor = threadMonitorExecutor;
    }

    public void execute() {
//        threadMonitorExecutor.startMonitoring();

        List<Callable<Void>> asyncTaskList = Arrays.stream(Constants.ReportItem.values())
                .parallel()
                .flatMap(reportItem -> Stream.of(
                        createCallable(() -> asyncWorkerService.executeRegisterAsync(reportItem)),
                        createCallable(() -> asyncWorkerService.executeHandlerAsync(reportItem)),
                        createCallable(() -> asyncWorkerService.executeCheckerAsync(reportItem))
                ))
                .collect(Collectors.toList());

        try {
            executorService.invokeAll(asyncTaskList);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread execution interrupted: {}", e.getMessage());
        }

        log.info("Thread task completed");
    }

    public void shutdown() {
        log.info("{} CollectorService shutdown", LOG_PREFIX);
        executorService.shutdown();
        asyncWorkerService.stopAsyncTasks();
        threadMonitorExecutor.stopMonitoring();
    }

    private Callable<Void> createCallable(ThrowingCallable callable) {
        return () -> {
            try {
                callable.run();
            } catch (Exception e) {
                handleException(e);
            }
            return null;
        };
    }

    private Callable<Void> stopCallable(StopCallable callable) {
        return () -> {
            try {
                callable.stop();
            } catch (Exception e) {
                handleException(e);
            }
            return null;
        };
    }

    private void handleException(Exception e) {
        log.error("{} handleException {}", LOG_PREFIX, e.getMessage(), e);
        // Your exception handling logic goes here
        e.printStackTrace();
        // You might want to log the exception or perform other actions
    }

}
