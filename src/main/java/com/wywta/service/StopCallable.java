/*
 * StopCallable.java 2024-01-17
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.service;

@FunctionalInterface
public interface StopCallable {
    void stop() throws Exception;
}
