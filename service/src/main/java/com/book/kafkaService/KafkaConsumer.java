package com.book.kafkaService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.book.entity.People;
import com.book.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * @author wangqianlong
 * @create 2019-11-28 13:38
 */

@Component
@Slf4j
public class KafkaConsumer {


    /**
     * 监听test 的topic，
     */
//    @KafkaListener(topics = "test")
//    public void listen(String context) {
//
//
//      /*  Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//
//        if (kafkaMessage.isPresent()) {
//            Object message = kafkaMessage.get();
//
//            log.info("Receive： +++++++++++++++ Topic:" + topic);
//            log.info("Receive： +++++++++++++++ Record:" + record);
//            log.info("Receive： +++++++++++++++ Message:" + message);
//        }*/
//
//        People people = JSON.parseObject(context, People.class);
//        log.info("接收到的对象为 {} " + people,people);
//
//    }

}
