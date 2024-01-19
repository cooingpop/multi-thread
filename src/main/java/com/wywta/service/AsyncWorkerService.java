/*
 * AsyncWorkerService.java 2024-01-16
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.service;

import com.wywta.core.DynamicThreadPoolManager;
import com.wywta.dao.StatusDAO;
import com.wywta.model.Constants;
import com.wywta.model.DummyData;
import com.wywta.model.Status;
import com.wywta.worker.MasterReportChecker;
import com.wywta.worker.MasterReportHandler;
import com.wywta.worker.MasterReportRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class AsyncWorkerService {

    private static final String LOG_PREFIX = "[AsyncWorkerService]";

    private final DynamicThreadPoolManager dynamicThreadPoolManager;

    private final MasterReportService masterReportService;

    private final DummyData dummyData;

    private final StatusDAO statusDAO;

    private final ThreadPoolTaskExecutor registerExecutor;


    private final ThreadPoolTaskExecutor checkerExecutor;

    private final ThreadPoolTaskExecutor handlerExecutor;

    private CopyOnWriteArrayList<MasterReportRegister> registerTasks = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<MasterReportChecker> checkerTasks = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<MasterReportHandler> handlerTasks = new CopyOnWriteArrayList<>();

    @Autowired
    public AsyncWorkerService(DynamicThreadPoolManager dynamicThreadPoolManager, MasterReportService masterReportService, DummyData dummyData, StatusDAO statusDAO,
                              @Qualifier("registerExecutor") ThreadPoolTaskExecutor registerExecutor,
                              @Qualifier("checkerExecutor") ThreadPoolTaskExecutor checkerExecutor,
                              @Qualifier("handlerExecutor") ThreadPoolTaskExecutor handlerExecutor) {
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
        this.masterReportService = masterReportService;
        this.dummyData = dummyData;
        this.statusDAO = statusDAO;
        this.registerExecutor = registerExecutor;
        this.checkerExecutor = checkerExecutor;
        this.handlerExecutor = handlerExecutor;
    }

    public void executeRegisterAsync(Constants.ReportItem reportItem) {
        log.info("{} executeRegisterAsync {}", LOG_PREFIX, reportItem);
        List<Status> statusList = dummyData.getStatusList();

        MasterReportRegister registerTask = new MasterReportRegister(statusList, masterReportService, reportItem, (long) reportItem.getValue(), dynamicThreadPoolManager, statusDAO);
        registerTasks.add(registerTask);
        registerExecutor.submit(registerTask);
    }

    public void executeCheckerAsync(Constants.ReportItem reportItem) {
        log.info("{} executeCheckerAsync {}", LOG_PREFIX, reportItem);
        List<Status> statusList = dummyData.getStatusList();

        MasterReportChecker checkerTask = new MasterReportChecker(statusList, masterReportService, reportItem, (long) reportItem.getValue(), dynamicThreadPoolManager);
        checkerTasks.add(checkerTask);
        checkerExecutor.submit(checkerTask);
    }

    public void executeHandlerAsync(Constants.ReportItem reportItem) {
        log.info("{} executeHandlerAsync {}", LOG_PREFIX, reportItem);
        List<Status> statusList = dummyData.getStatusList();

        MasterReportHandler handlerTask = new MasterReportHandler(statusList, masterReportService, reportItem, (long) reportItem.getValue());
        handlerTasks.add(handlerTask);
        handlerExecutor.submit(handlerTask);
    }

    public void stopAsyncTasks() {
        registerTasks.forEach(MasterReportRegister::stop);
        checkerTasks.forEach(MasterReportChecker::stop);
        handlerTasks.forEach(MasterReportHandler::stop);
    }

}
