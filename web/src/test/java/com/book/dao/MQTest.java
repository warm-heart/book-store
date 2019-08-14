package com.book.dao;

import com.book.MQ.MQService;


import com.book.entity.User;
import com.book.utils.JsonUtil;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangqianlong
 * @create 2019-08-09 18:54
 */

public class MQTest extends StartApplicationTests {
    @Autowired
    RabbitTemplate template;
    @Autowired
    MQService mqService;

    @Test
    public void test() throws InterruptedException {
        AtomicInteger flag = new AtomicInteger(1);
      //  while (true) {
            User user = new User();
            user.setUserId(String.valueOf(flag.incrementAndGet()));
            user.setUserPassword("123");
            user.setUserName("cooper");
            user.setUserEmail("bookOrder");
            List<User> users = new ArrayList<>(4);
            users.add(user);
            users.add(user);
            users.add(user);
            users.add(user);
            Thread.sleep(2000);
            String json = JsonUtil.toJson(users);
            mqService.send(json);
        //}
    }
}
