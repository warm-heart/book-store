package com.book.MQ;



import com.book.entity.User;

import com.book.utils.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
       // rabbitTemplate.convertAndSend(RabbitMqConfig.DIRECT_EXCHANGE, "bookOrder", json);
    }

   /* @RabbitListener(queues = RabbitMqConfig.DIRECT_QUEUE1)
    public void receiveTopic1(String message) throws InterruptedException {
       // Thread.sleep(2000);
        List<User> users = (List<User>) JsonUtil.fromJson(message, new TypeReference<List<User>>(){});
        System.out.println("【receiveTopic1监听到消息】" +users);
    }*/

  /*  @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = RabbitMqConfig.DIRECT_QUEUE1,autoDelete = "true",durable = "false"),
                    exchange = @Exchange(value = RabbitMqConfig.DIRECT_EXCHANGE),
                    key = "bookOrder"
            ))
    public void receiveTopic2(String message) {
        List<User> users = (List<User>) JsonUtil.fromJson(message, new TypeReference<List<User>>() {
        });
        System.out.println("【receiveTopic1监听到消息】" + users);
    }*/

}
