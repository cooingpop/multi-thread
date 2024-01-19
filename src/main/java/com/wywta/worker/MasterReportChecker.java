/*
 * MasterReportChecker.java 2024-01-16
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.worker;

import com.wywta.core.DynamicThreadPoolManager;
import com.wywta.model.Constants;
import com.wywta.model.Status;
import com.wywta.service.MasterReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class MasterReportChecker implements Runnable {
    private static final String LOG_PREFIX = "[CHECKER]";

    private static final int THREAD_COUNT = 1;

    private Long minWaitSeconds = 90 * 1000L;

    private Constants.ReportItem reportItem;

    private final MasterReportService masterReportService;

    private boolean isInterrupted = false;

    private List<Status> statusList;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(THREAD_COUNT);

    private ScheduledFuture<?> scheduledFuture;

    private final DynamicThreadPoolManager dynamicThreadPoolManager;

    public MasterReportChecker (List<Status> statusList, MasterReportService masterReportService, Constants.ReportItem reportItem, Long minWaitSeconds, DynamicThreadPoolManager dynamicThreadPoolManager) {
        this.statusList = statusList;
        this.masterReportService = masterReportService;
        this.reportItem = reportItem;
        this.minWaitSeconds = (minWaitSeconds / 2) * 1000L;
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
    }

    @Override
    public void run() {
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("{} Start MasterReportChecker {}", LOG_PREFIX, Thread.currentThread().getName());
                long start = System.currentTimeMillis();
                List<Status> targetList = statusList;
                List<CompletableFuture<Integer>> futuresList = targetList.stream()
                        .filter(f -> StringUtils.equals("REGIST", f.getStatus()) && Objects.isNull(f.getResult())
                        || StringUtils.equals("NONE", f.getStatus()) && StringUtils.equals("SUCCESS", f.getResult()))
                        .map(target -> masterReportService.asyncChecker(reportItem, target))
                        .toList();

                // 성공한 CompletableFuture 개수를 계산
                int successCount = futuresList.stream()
                        .map(CompletableFuture::join)
                        .reduce(0, Integer::sum);

                // 이제 successCount를 사용하여 다른 작업을 수행할 수 있습니다.
                long elapsed = System.currentTimeMillis() - start;
                log.info("{} Item='{}', {}개(성공{}개) 완료", LOG_PREFIX, reportItem.name(), futuresList.size(), successCount);


                try {
                    if (Thread.currentThread().isInterrupted() || isInterrupted) {
                        throw new InterruptedException();
                    } else {
                        if (elapsed < minWaitSeconds) {
                            long sleep = minWaitSeconds - elapsed;
                            log.info("{} {} {}ms만큼 대기합니다. ########################################", LOG_PREFIX, reportItem, sleep);
                            Thread.sleep(sleep);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            } catch (RejectedExecutionException e) {
                log.error("{}CompletableFuture 예외 발생: Task rejected by ExecutorService: {}", LOG_PREFIX, e.getMessage());
                dynamicThreadPoolManager.monitorThreadPoolSize("checkerExecutor");
            } catch (Exception e) {
                log.error("{}CompletableFuture 예외 발생: {}", LOG_PREFIX, e.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (Objects.nonNull(scheduledFuture)) {
            // 예약된 작업 취소
            scheduledFuture.cancel(true);
            scheduler.shutdown();
        }
        log.info("{} - {} 종료합니다.", LOG_PREFIX, reportItem);
    }

}
