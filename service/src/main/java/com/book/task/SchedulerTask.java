package com.book.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wangqianlong
 * @create 2019-08-19 10:28
 */
@Component
public class SchedulerTask {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ThreadLocal<DateFormat> threadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    @Scheduled(cron = "0 0 0-23 * * ? ")
    public void reportCurrentTime() {

        System.out.println("现在时间：" + formatter.format(LocalDateTime.now()));
    }
}
