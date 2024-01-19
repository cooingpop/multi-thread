/*
 * DummyData.java 2024-01-16
 *
 * @author joonyeong.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.model;

import com.wywta.model.Status;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class DummyData {

    private final CopyOnWriteArrayList<Status> statusList = new CopyOnWriteArrayList<>(initializeStatusList());

    public List<Status> getStatusList() {
        return statusList;
    }

    public static List<Status> initializeStatusList() {
        return IntStream.rangeClosed(100, 20000)
                .mapToObj(i -> {
                    Status status = new Status();
                    status.setAdsrId(i);
                    status.setStatus("NONE");
                    status.setResult(null);
                    return status;
                })
                .collect(Collectors.toList());
    }
}



