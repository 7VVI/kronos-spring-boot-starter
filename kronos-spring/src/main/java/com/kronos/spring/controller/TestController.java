package com.kronos.spring.controller;

import com.kronos.spring.annotation.ConvertTime;
import com.kronos.spring.annotation.Time;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * @author zhangyh
 * @Date 2025/5/26 9:54
 * @desc
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @ConvertTime
    @GetMapping("/date")
    public ResponseEntity<String> getDate(@Time @RequestParam("date")@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime date) {
        System.out.println(date);
        return  ResponseEntity.ok("hello world");
    }

    @ConvertTime
    @PostMapping("/date")
    public ResponseEntity<String> postDate(@RequestBody Data date) {
        System.out.println(date);
        return  ResponseEntity.ok("hello world");
    }
}
