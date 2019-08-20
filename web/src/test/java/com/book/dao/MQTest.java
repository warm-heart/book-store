package com.book.dao;

import com.alibaba.fastjson.JSON;
import com.book.MQ.EsMqService;
import com.book.MQ.MQService;


import com.book.entity.Role;
import com.book.entity.User;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author wangqianlong
 * @create 2019-08-09 18:54
 */

public class MQTest extends StartApplicationTests {
    @Autowired
    RabbitTemplate template;
    @Autowired
    MQService mqService;
    @Autowired
    EsMqService esMqService;

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < 100; i++) {


            Role role = new Role();
            role.setUserId("1");
            role.setRoleId(i);
            role.setRoleName("123");
            mqService.send(JSON.toJSONString(role));
            Thread.sleep(2000);
        }
    }

}
