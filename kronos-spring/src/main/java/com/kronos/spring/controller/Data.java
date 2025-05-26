package com.kronos.spring.controller;

import com.kronos.spring.annotation.Time;

/**
 * @author zhangyh
 * @Date 2025/5/26 9:55
 * @desc
 */
@lombok.Data
public class Data {

    @Time
    private String date;
}
