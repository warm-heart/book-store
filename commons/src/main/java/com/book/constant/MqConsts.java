package com.book.constant;

/**
 * @author wangqianlong
 * @create 2019-08-12 17:03
 */

public class MqConsts {

    //redirect模式

    //交换机
    public static final String BOOK_DIRECT_EXCHANGE = "BookDirectExchange";

    //队列
    public static final String BOOK_ORDER_QUEUE = "BookOrderQueue";
    public static final String DIRECT_QUEUE2 = "direct.queue2";
    public static final String ES_ADD_BOOK_QUEUE = "EsAddBookQueue";


    //routing key
    public static final String ES_ADD_BOOK_ROUTING_KEY = "EsAddBook";
    public static final String ROUTING_KEY2 = "routingKey2";
    public static final String BOOK_ORDER_ROUTING_KEY = "bookOrder";




//    topic
//    public static final String TOPIC_QUEUE1 = "topic.queue1";
//    public static final String TOPIC_QUEUE2 = "topic.queue2";
//    public static final String TOPIC_EXCHANGE = "topic.exchange";
//
//    fanout
//    public static final String FANOUT_QUEUE1 = "fanout.queue1";
//    public static final String FANOUT_QUEUE2 = "fanout.queue2";
//    public static final String FANOUT_EXCHANGE = "fanout.exchange";
}
