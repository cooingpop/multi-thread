/*
 * ThrowingCallable.java 2024-01-16
 *
 * @author joonyeong.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.service;

/**
 * 람다 표현식을 사용하여 간단한 작업을 수행하고 예외를 처리하기 위한 편의성을 제공하기 위한 사용자 정의 인터페이스일 뿐
 */
@FunctionalInterface
public interface ThrowingCallable {
    void run() throws Exception;
}
