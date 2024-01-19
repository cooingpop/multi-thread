/*
 * MasterReportHandler.java 2024-01-16
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.worker;

import com.wywta.model.Constants;
import com.wywta.model.Status;
import com.wywta.service.MasterReportService;
import com.wywta.util.CompletableFutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class MasterReportHandler implements Runnable {
    private static final String LOG_PREFIX = "[HANDLER]";

    private static final int THREAD_COUNT = 1;

    private Long minWaitSeconds = 30 * 1000L;

    private Constants.ReportItem reportItem;

    private final MasterReportService masterReportService;

    private List<Status> statusList;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(THREAD_COUNT);

    private ScheduledFuture<?> scheduledFuture;

    public MasterReportHandler (List<Status> statusList, MasterReportService masterReportService, Constants.ReportItem reportItem, Long minWaitSeconds) {
        this.statusList = statusList;
        this.masterReportService = masterReportService;
        this.reportItem = reportItem;
    }

    @Override
    public void run() {
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("{} Start MasterReportHandler {}", LOG_PREFIX, Thread.currentThread().getName());
                long start = System.currentTimeMillis();
                List<Status> targetList = statusList;
                List<CompletableFuture<Status>> futures = targetList.stream()
                        .filter(f -> StringUtils.equals("BUILT", f.getStatus()) && StringUtils.equals("SUCCESS", f.getResult()))
                        .map(target -> masterReportService.asyncHandler(reportItem, target))
                        .toList();

                CompletableFutureUtils.getCompletableFutureAllOfJobs(start, futures, log, LOG_PREFIX, reportItem, minWaitSeconds);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("{}CompletableFuture 예외 발생: {}", LOG_PREFIX, e.getMessage());
            }
        }, 0, minWaitSeconds, TimeUnit.MILLISECONDS);
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
