package com.book.MQ;


import com.book.constant.MqConsts;
import com.book.entity.User;
import com.book.search.BookIndexTemplate;
import com.book.search.BookSearchService;
import com.book.service.BookService;
import com.book.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void receiveTopic1(String message) {

        BookIndexTemplate bookIndexTemplate = (BookIndexTemplate) JsonUtil.fromJson(message, BookIndexTemplate.class);
        log.info("【ES_ADD_BOOK_QUEUE接收到消息】 {}", bookIndexTemplate);
        bookSearchService.add(bookIndexTemplate);
    }

}
