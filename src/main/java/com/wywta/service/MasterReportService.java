/*
 * MasterReportService.java 2024-01-16
 *
 * @author joonyeong.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.service;

import com.wywta.model.Constants;
import com.wywta.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MasterReportService {
    private static final String LOG_PREFIX = "[ReportService]";

    @Async("registerExecutor")
    public CompletableFuture<Status> asyncRegister(Constants.ReportItem reportItem, Status status) {
        log.info("{} Register thread: {} adsrId {} {} {} ", LOG_PREFIX, reportItem.name(), status.getAdsrId(), Thread.currentThread().getName(), status);
        status.setStatus("REGIST");
        return CompletableFuture.completedFuture(status);
    }

    @Async("handlerExecutor")
    public CompletableFuture<Status> asyncHandler(Constants.ReportItem reportItem, Status status) {
        log.info("{} Handler thread: {} adsrId {} {} {} ", LOG_PREFIX, reportItem.name(), status.getAdsrId(), Thread.currentThread().getName(), status);

        status.setStatus("NONE");

        log.info("{}{}광고주(item={}) 데이터 입력완료", LOG_PREFIX, status.getAdsrId(), reportItem.name());
        return CompletableFuture.completedFuture(status);
    }

    @Async("checkerExecutor")
    public CompletableFuture<Integer> asyncChecker(Constants.ReportItem reportItem, Status status) {
        log.info("{} Checker before thread: {} adsrId {} {} {} ", LOG_PREFIX, reportItem.name(), status.getAdsrId(), Thread.currentThread().getName(), status);

        if (Objects.isNull(status)) {
            return CompletableFuture.completedFuture(0);
        }

        if (StringUtils.equals("BUILT", status.getStatus()) && StringUtils.equals("SUCCESS", status.getResult())) {
            return CompletableFuture.completedFuture(1);
        }

       int result = 0;
        if (StringUtils.equals("REGIST", status.getStatus()) && Objects.isNull(status.getResult())) {
            result = 1;
            status.setStatus("BUILT");
            status.setResult("SUCCESS");
        } else if (StringUtils.equals("NONE", status.getStatus()) && StringUtils.equals("SUCCESS", status.getResult())) {
            result = 1;
            status.setResult(null);
        }
        else if (StringUtils.equals("FAIL", status.getStatus()) ) {
            result = 1;
            status.setStatus("NONE");
            status.setResult(null);
        } else {
            result = 1;
            status.setResult("FAIL");
        }

        log.info("{} Checker current thread: {} adsrId {} {} {} ", LOG_PREFIX, reportItem.name(), status.getAdsrId(), Thread.currentThread().getName(), status);
        return CompletableFuture.completedFuture(result);
    }

}
