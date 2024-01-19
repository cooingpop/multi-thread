/*
 * MasterReportRegister.java 2024-01-16
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.worker;

import com.wywta.core.DynamicThreadPoolManager;
import com.wywta.dao.StatusDAO;
import com.wywta.model.Constants;
import com.wywta.model.Status;
import com.wywta.service.MasterReportService;
import com.wywta.util.CompletableFutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class MasterReportRegister implements Runnable {
    private static final String LOG_PREFIX = "[REGISTER]";

    private static final int THREAD_COUNT = 1;

    private Long minWaitSeconds = 90 * 1000L;

    private Constants.ReportItem reportItem;

    private final MasterReportService masterReportService;

    private List<Status> statusList;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(THREAD_COUNT);

    private ScheduledFuture<?> scheduledFuture;

    private final DynamicThreadPoolManager dynamicThreadPoolManager;

    private final StatusDAO statusDAO;

    public MasterReportRegister (List<Status> statusList, MasterReportService masterReportService
            , Constants.ReportItem reportItem, Long minWaitSeconds, DynamicThreadPoolManager dynamicThreadPoolManager, StatusDAO statusDAO) {
        this.statusList = statusList;
        this.masterReportService = masterReportService;
        this.reportItem = reportItem;
        this.minWaitSeconds = minWaitSeconds * 1000L;
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
        this.statusDAO = statusDAO;
    }

    @Override
    public void run() {
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("{} Start MasterReportRegister {}", LOG_PREFIX, Thread.currentThread().getName());
                long start = System.currentTimeMillis();
                List<Status> targetList = statusList;

                CompletableFuture[] futures = targetList.stream().filter(f -> StringUtils.equals("NONE", f.getStatus()) && Objects.isNull(f.getResult()))
                        .map(target -> masterReportService.asyncRegister(reportItem, target))
                        .map(future -> future.thenApply(statusDAO::insertStatus))
                        .toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(futures).join();

//              List<CompletableFuture<Integer>> futures = targetList.stream().filter(f -> StringUtils.equals("NONE", f.getStatus()) && Objects.isNull(f.getResult()))
//                        .map(target -> masterReportService.asyncRegister(reportItem, target))
//                        .map(future -> future.thenApply(fn -> statusDAO.insertStatus(fn)))
//                        .toList();

//                CompletableFutureUtils.getCompletableFutureAllOfJobs(start, futures, log, LOG_PREFIX, reportItem, minWaitSeconds);
                long successCount = Arrays.stream(futures).filter(r -> {
                    try {
                        return (Integer)r.get() != 0;
                    } catch (Exception ex) {return false;}
                }).count();

                long elapsed = System.currentTimeMillis() - start;
                log.info("{}Item='{}', {}개(성공{}개) 완료, elapsed : {} ms", LOG_PREFIX, reportItem.name(), futures.length, successCount, elapsed);


                if (!Thread.currentThread().isInterrupted()) {
                    if (elapsed < minWaitSeconds) {
                        long sleep = minWaitSeconds - elapsed;
                        log.info("{} {} {}ms만큼 대기합니다. ########################################", LOG_PREFIX, reportItem, sleep);
                        Thread.sleep(sleep);
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RejectedExecutionException e) {
                log.error("{}CompletableFuture 예외 발생: Task rejected by ExecutorService: {}", LOG_PREFIX, e.getMessage());
                dynamicThreadPoolManager.monitorThreadPoolSize("registerExecutor");
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