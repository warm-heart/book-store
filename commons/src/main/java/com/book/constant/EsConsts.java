package com.book.constant;

/**
 * @author wangqianlong
 * @create 2019-08-07 21:16
 */

public class EsConsts {


    public static String HOSTS = "127.0.0.1"; // 集群地址，多个用,隔开
    public static int PORT = 9200; // 使用的端口号
    public static String SCHEMA = "http"; // 使用的协议

    public static int CONNECT_TIMEOUT = 1000; // 连接超时时间
    public static int SOCKET_TIMEOUT = 30000; // 连接超时时间
    public static int CONNECTION_REQUEST_TIMEOUT = 500; // 获取连接的超时时间
    public static int MAX_CONNECT_NUM = 100; // 最大连接数
    public static int MAX_CONNECT_PER_ROUTE = 100; // 最大路由连接数

    public static final String INDEX_NAME = "bookstore";

    public static final String TYPE = "book";

    public static final String BOOK_DESCRIPTION = "bookDescription";

    public static final String CATEGORY_NAME = "categoryName";


}
