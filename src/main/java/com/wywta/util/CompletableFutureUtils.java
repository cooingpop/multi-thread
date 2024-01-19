/*
 * CompletableFutureUtils.java 2024-01-17
 *
 * @author joonyeong.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.util;

import com.wywta.model.Constants;
import com.wywta.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class CompletableFutureUtils {


    public static void getCompletableFutureAllOfJobs(long start, List<CompletableFuture<Status>> futures, Logger log, String logPrefix, Constants.ReportItem reportItem, Long minWaitSeconds) throws InterruptedException {
        long elapsed;
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        log.info("{} {} thread: {} join", logPrefix, reportItem.name(), Thread.currentThread().getName());
        allOf.join();

        elapsed = System.currentTimeMillis() - start;

        if (!Thread.currentThread().isInterrupted()) {
            if (elapsed < minWaitSeconds) {
                long sleep = minWaitSeconds - elapsed;
                log.info("{} {} {}ms만큼 대기합니다. ########################################", logPrefix, reportItem, sleep);
                Thread.sleep(sleep);
            }
        }
    }
}
