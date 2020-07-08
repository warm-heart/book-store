package com.book.mqservice;


import com.book.constant.MqConsts;
import com.book.entity.Role;

import com.book.utils.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
    public void receiveTopic1(Channel channel, Message message) throws IOException, InterruptedException {
        try {


            Role role = (Role) JsonUtil.fromJson((byte[]) message.getBody(), new TypeReference<Role>() {
            });

            System.out.println("【MQService接收消息转化为实体类】" + role);
            System.out.println("【receiveTopic1监听到消息】" + message);
            System.out.println("【消息Id是】" + message.getMessageProperties().getMessageId());

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false  );

            //System.out.println("消息Id是：" + message.getHeaders().get(AmqpHeaders.MESSAGE_ID));
            // channel.basicAck((Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG), false);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            //channel.basicAck((Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG), false);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

    }

}
