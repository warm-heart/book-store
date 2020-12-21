package com.zhangchu.datacenter.config;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangchu.datacenter.annotation.Index;
import com.zhangchu.datacenter.constant.EsConstants;
import com.zhangchu.datacenter.dao.IndexFieldInfoDao;
import com.zhangchu.datacenter.entity.IndexFieldInfo;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wql
 * @Description
 * @create 2020-11-16 11:18
 */
@Configuration

public class EsAutoConfig implements ApplicationContextAware, SmartInitializingSingleton {
    private static final Logger log = LoggerFactory.getLogger(EsAutoConfig.class);

    @Autowired
    private Environment environment;
    private ApplicationContext applicationContext;

    @Value("${elaticsearch.hosts}")
    List<String> hosts;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        HttpHost[] nodes = getNodes();
        RestClientBuilder builder = RestClient.builder(nodes);
        // 异步httpclient连接延时配置
        builder.setRequestConfigCallback(new RequestConfigCallback() {
            @Override
            public Builder customizeRequestConfig(Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(EsConstants.CONNECT_TIMEOUT);
                requestConfigBuilder.setSocketTimeout(EsConstants.SOCKET_TIMEOUT);
                requestConfigBuilder.setConnectionRequestTimeout(EsConstants.CONNECTION_REQUEST_TIMEOUT);
                return requestConfigBuilder;
            }
        });

        // 异步httpclient连接数配置
        builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.setMaxConnTotal(EsConstants.MAX_CONNECT_NUM);
                httpClientBuilder.setMaxConnPerRoute(EsConstants.MAX_CONNECT_PER_ROUTE);
                return httpClientBuilder;
            }
        });
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;

    }

    public HttpHost[] getNodes() {
        List<HttpHost> hostList = new ArrayList<>();
        for (String host : hosts) {
            String[] node = host.split(":");
            String ip = node[0].trim();
            Integer port = Integer.parseInt(node[1]);
            hostList.add(new HttpHost(ip, port, EsConstants.SCHEMA));
        }
        return hostList.toArray(new HttpHost[0]);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        //todo 更新索引信息全局开关
        // if ("true".equals(environment.getProperty("reFreshIndexField"))) {
        //
        // }
        if (false) {
            Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(Index.class);
            for (Map.Entry bean : beans.entrySet()) {
                Index index = bean.getValue().getClass().getAnnotation(Index.class);
                String indexName = index.indexName();
                if (!StringUtils.hasText(indexName)) {
                    continue;
                }
                refreshIndexFieldInfo(indexName);
            }
        }
    }

    private void refreshIndexFieldInfo(String indexName) {
        RestHighLevelClient restHighLevelClient = applicationContext.getBean(RestHighLevelClient.class);
        IndexFieldInfoDao indexFieldInfoDao = applicationContext.getBean(IndexFieldInfoDao.class);
        try {
            //获取索引字段信息
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            GetIndexResponse response = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
            Map<String, MappingMetadata> mappings = response.getMappings();
            MappingMetadata mappingMetadata = mappings.get(indexName);
            Map<String, Object> sourceAsMap = mappingMetadata.getSourceAsMap();
            Map<String, Object> map = (Map<String, Object>) sourceAsMap.get("properties");

            //删除数据库索引字段信息
            QueryWrapper<IndexFieldInfo> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(IndexFieldInfo::getIndexName, indexName);
            indexFieldInfoDao.delete(wrapper);

            //保存新的索引字段信息
            for (Map.Entry entry : map.entrySet()) {
                Map<String, Object> typeMap = (Map<String, Object>) entry.getValue();
                String fieldType = (String) typeMap.get("type");

                Integer isX = 0;
                Integer isTopHits = 0;
                Integer isYCompute = 0;
                Integer isYCount = 0;
                Integer isSort = 1;

                if (EsConstants.CAN_X_AXIS.contains(fieldType)) {
                    isX = 1;
                }
                if (!fieldType.equals(EsConstants.TEXT)) {
                    isTopHits = 1;
                }

                if (EsConstants.CAN_Y_COMPUTE.contains(fieldType)) {
                    isYCompute = 1;
                }
                if (!fieldType.equals(EsConstants.TEXT)) {
                    isYCount = 1;
                }

                if (typeMap.containsKey("doc_values")) {
                    boolean docValues = (boolean) typeMap.get("doc_values");
                    if (!docValues) {
                        isX = 0;
                        isTopHits = 0;
                        isYCompute = 0;
                        isYCount = 0;
                        isSort = 0;
                    }
                }
                IndexFieldInfo indexFieldInfo = new IndexFieldInfo();
                indexFieldInfo.setIndexName(indexName);
                indexFieldInfo.setFieldType(fieldType);
                indexFieldInfo.setFieldName((String) entry.getKey());
                indexFieldInfo.setIsX(isX);
                indexFieldInfo.setIsTopHits(isTopHits);
                indexFieldInfo.setIsYCompute(isYCompute);
                indexFieldInfo.setIsYCount(isYCount);
                indexFieldInfo.setIsSort(isSort);

                indexFieldInfoDao.insert(indexFieldInfo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
