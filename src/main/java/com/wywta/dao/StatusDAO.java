/*
 * StatusDAO.java 2024-01-19
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.dao;

import com.wywta.model.Status;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class StatusDAO {

    private static final String LOG_PREFIX = "[StatusDAO]";

    public int insertStatus(Status status) {
        if (StringUtils.equals("FAIL", status.getResult())) {
//            return 0;
        }

        return 1;

    }
}
