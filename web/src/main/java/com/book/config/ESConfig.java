package com.book.config;


import com.book.constant.EsConsts;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.ArrayList;

/**
 * @author wangqianlong
 * @create 2019-08-07 17:15
 */
@Configuration
public class ESConfig {



    private static ArrayList<HttpHost> hostList = null;

    static {
        hostList = new ArrayList<>();
        String[] hostStrs = EsConsts.HOSTS.split(",");
        for (String host : hostStrs) {
            hostList.add(new HttpHost(host, EsConsts.PORT, EsConsts.SCHEMA));
        }
    }

    @Bean
    public RestHighLevelClient client() {
        RestClientBuilder builder = RestClient.builder(hostList.toArray(new HttpHost[0]));
        // 异步httpclient连接延时配置
        builder.setRequestConfigCallback(new RequestConfigCallback() {
            @Override
            public Builder customizeRequestConfig(Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(EsConsts.CONNECT_TIMEOUT);
                requestConfigBuilder.setSocketTimeout(EsConsts.SOCKET_TIMEOUT);
                requestConfigBuilder.setConnectionRequestTimeout(EsConsts.CONNECTION_REQUEST_TIMEOUT);
                return requestConfigBuilder;
            }
        });

        // 异步httpclient连接数配置
        builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.setMaxConnTotal(EsConsts.MAX_CONNECT_NUM);
                httpClientBuilder.setMaxConnPerRoute(EsConsts.MAX_CONNECT_PER_ROUTE);
                return httpClientBuilder;
            }
        });
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;

    }
}

