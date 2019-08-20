package com.book.MQ;


import com.book.constant.MqConsts;
import com.book.entity.Role;
import com.book.entity.User;

import com.book.utils.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author wangqianlong
 * @create 2019-08-09 18:25
 */
@Service
public class MQService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String json) {
        rabbitTemplate.convertAndSend(MqConsts.BOOK_DIRECT_EXCHANGE, MqConsts.ROUTING_KEY2, json);
    }

    @RabbitListener(queues = MqConsts.DIRECT_QUEUE2)
    public void receiveTopic1(String json, Channel channel, Message message) throws IOException {

        Role role = (Role) JsonUtil.fromJson(json, Role.class);
        System.out.println(role);
        System.out.println("【receiveTopic1监听到消息】" + json);
        //throw new RuntimeException();
    }

}
