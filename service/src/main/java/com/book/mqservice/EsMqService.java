package com.book.mqservice;


import com.book.constant.MqConsts;
import com.book.search.BookIndexTemplate;
import com.book.search.BookSearchService;
import com.book.utils.JsonUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * @author wangqianlong
 * @create 2019-08-12 11:03
 */
@Service
@Slf4j
public class EsMqService {
    @Autowired
    BookSearchService bookSearchService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String json) {
        rabbitTemplate.convertAndSend(MqConsts.BOOK_DIRECT_EXCHANGE, MqConsts.ES_ADD_BOOK_ROUTING_KEY, json);
    }

    @RabbitListener(queues = MqConsts.ES_ADD_BOOK_QUEUE)
    public void EsAddBookReceiver(String json, Channel channel, Message message) throws IOException {
        try {
            //TODO 判断消息是否被消费如果被消费则直接ACK  消息幂等性处理 手动ACK为了流量削峰
            BookIndexTemplate bookIndexTemplate = (BookIndexTemplate) JsonUtil.fromJson(json, BookIndexTemplate.class);
            log.info("【ES_ADD_BOOK_QUEUE接收到消息】 {}", bookIndexTemplate);
            bookSearchService.add(bookIndexTemplate);
            //TODO 消费消息成功 更改消息标记状态为已消费
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //TODO 消费消费失败 更改消息标记和重试次数 等待分布式定时任务重新发布消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

    }

}
