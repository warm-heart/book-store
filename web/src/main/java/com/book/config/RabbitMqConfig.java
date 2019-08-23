package com.book.config;


import com.book.constant.MqConsts;
import com.book.entity.Role;
import com.book.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

/**
 * @author wangqianlong
 * @create 2019-08-09 17:43
 */
@Configuration
@Slf4j
public class RabbitMqConfig {


    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);


        // 消息是否成功发送到Exchange
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                String msgId = correlationData.getId();

                //TODO  更改消息数据库中的消息标记状态为已发送
                log.info("消息成功发送到Exchange correlationDataID {}", msgId);
            } else {
                //TODO  更改消息数据库中的消息标记状态为发送失败，定时任务稍后重新发送
                log.info("消息发送到Exchange失败, {}, cause: {}", correlationData, cause);
            }
        });

        // 触发setReturnCallback回调必须设置mandatory=true, 否则Exchange没有找到Queue就会丢弃掉消息, 而不会触发回调
        rabbitTemplate.setMandatory(true);
        // 消息是否从Exchange路由到Queue, 注意: 这是一个失败回调, 只有消息从Exchange路由到Queue失败才会回调这个方法
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            Role role = (Role) JsonUtil.fromJson(message.getBody(), new TypeReference<Role>() {
            });
            //TODO 更改消息数据库中的消息标记状态
            log.info("消息从Exchange路由到Queue失败: exchange: {}, route: {}, replyCode: {}, replyText: {}, message: {} ,role:{}", exchange, routingKey, replyCode, replyText, message, role);
        });

        return rabbitTemplate;
    }


    /**
     * direct模式     *
     * 消息中的路由键（routing key）如果和 Binding 中的 binding key 一致，
     * 交换器就将消息发到对应的队列中。路由键与队列名完全匹配
     */
    @Bean
    public Queue bookOrderQueue() {

        return new Queue(MqConsts.BOOK_ORDER_QUEUE);
    }

    @Bean
    public Queue directQueue2() {
        return new Queue(MqConsts.DIRECT_QUEUE2);
    }

    @Bean
    public Queue EsAddBookQueue() {
        return new Queue(MqConsts.ES_ADD_BOOK_QUEUE);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(MqConsts.BOOK_DIRECT_EXCHANGE);
    }

    /**
     * @return queue1接受routingKey1的数据
     */
    @Bean
    public Binding BookOrderQueueBinding() {
        return BindingBuilder.bind(bookOrderQueue()).to(directExchange()).with(MqConsts.BOOK_ORDER_ROUTING_KEY);
    }

    /**
     * @return queue2接受routingKey2的数据
     */
    @Bean
    public Binding directBinding2() {
        return BindingBuilder.bind(directQueue2()).to(directExchange()).with(MqConsts.ROUTING_KEY2);
    }


    /**
     * @return 添加图书队列与key绑定
     */
    @Bean
    public Binding EsAddBookQueueBinding() {
        return BindingBuilder.bind(EsAddBookQueue()).to(directExchange()).with(MqConsts.ES_ADD_BOOK_ROUTING_KEY);
    }


    /**
     * Topic模式
     */
//    @Bean
//    public Queue topicQueue1() {
//        return new Queue(TOPIC_QUEUE1);
//    }
//
//    @Bean
//    public Queue topicQueue2() {
//        return new Queue(TOPIC_QUEUE2);
//    }
//
//    @Bean
//    public TopicExchange topicExchange() {
//        return new TopicExchange(TOPIC_EXCHANGE);
//    }
//
//
//    @Bean
//    public Binding topicBinding1() {
//        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("lzc.message");
//    }
//
//    @Bean
//    public Binding topicBinding2() {
//        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("lzc.#");
//    }

    /**
     * Fanout模式
     * Fanout 就是我们熟悉的广播模式或者订阅模式，给Fanout交换机发送消息，
     * 绑定了这个交换机的所有队列都收到这个消息。
     * 两个queue 接受同一个交换机中的数据
     */
//    @Bean
//    public Queue fanoutQueue1() {
//        return new Queue(FANOUT_QUEUE1);
//    }
//
//    @Bean
//    public Queue fanoutQueue2() {
//        return new Queue(FANOUT_QUEUE2);
//    }
//
//    @Bean
//    public FanoutExchange fanoutExchange() {
//        return new FanoutExchange(FANOUT_EXCHANGE);
//    }
//
//    @Bean
//    public Binding fanoutBinding1() {
//        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
//    }
//
//    @Bean
//    public Binding fanoutBinding2() {
//        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
//    }


    /**
     * 定义消息转换实例  转化成 JSON 传输  传输实体就可以不用实现序列化
     */
/*    @Bean
    public MessageConverter integrationEventMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }*/

}


