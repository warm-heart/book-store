package com.book.dao;

import com.book.mqservice.EsMqService;
import com.book.mqservice.MQService;


import com.book.constant.MqConsts;
import com.book.entity.Role;
import com.book.utils.JsonUtil;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author wangqianlong
 * @create 2019-08-09 18:54
 */

public class MQTest extends StartApplicationTests {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    MQService mqService;
    @Autowired
    EsMqService esMqService;

    @Test
    public void test() throws InterruptedException {


        Role role = new Role();
        role.setUserId("1");
        role.setRoleId(123);
        role.setRoleName("123");


       /* rabbitTemplate.convertAndSend(MqConsts.BOOK_DIRECT_EXCHANGE, MqConsts.ROUTING_KEY2,
                JsonUtil.toJson(role), new CorrelationData("1213eq13"));*/


        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getHeaders().put("desc", "信息描述");
        messageProperties.getHeaders().put("type", "自定义消息类型");

        messageProperties.setMessageId("数据库主键ID123456");

        //设置消息过期时间
        messageProperties.setExpiration("10000");
        Message message = new Message(JsonUtil.toJson(role).getBytes(), messageProperties);
        rabbitTemplate.convertAndSend(MqConsts.BOOK_DIRECT_EXCHANGE, MqConsts.ROUTING_KEY2,
                message, new CorrelationData("数据库主键ID123456"));
    }

}
