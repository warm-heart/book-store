package com.zhangchu.datacenter.es.service.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhangchu.datacenter.annotation.Index;
import com.zhangchu.datacenter.constant.EsConstants;
import com.zhangchu.datacenter.dto.QueryField;
import com.zhangchu.datacenter.dto.RangeField;
import com.zhangchu.datacenter.dto.SearchResult;
import com.zhangchu.datacenter.enums.CalculateType;
import com.zhangchu.datacenter.es.index.base.BaseIndexTemplate;

import com.zhangchu.datacenter.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.histogram.*;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.ParsedDateRange;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wql
 * @Description
 * @create 2020-12-02 18:21
 */
@Slf4j
public class EsBaseService<T extends BaseIndexTemplate> {

    public void updateIndexSettings(String indexName) {
        UpdateSettingsRequest request = new UpdateSettingsRequest(indexName);
        String settingKey = "index.routing.allocation.require.temperature";
        int settingValue = 0;
        Settings settings =
                Settings.builder()
                        .put(settingKey, settingValue)
                        .build();
        request.settings(settings);
        try {
            AcknowledgedResponse updateSettingsResponse =
                    getRestHighLevelClient().indices().putSettings(request, RequestOptions.DEFAULT);
            if (updateSettingsResponse.isAcknowledged()) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createIndex() {
        String index = getIndex();
        String indexFileName = "es/" + index + ".json";
        URL url = this.getClass().getClassLoader().getResource(indexFileName);
        if (null == url) {
            log.info("请检查文件是否存在,{}", indexFileName);
        }
        String fileName = url.getFile();
        FileInputStream in = null;
        StringBuilder indexContent = new StringBuilder();
        try {
            in = new FileInputStream(fileName);
            FileChannel fileChannel = in.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(1024);
            int bytesRead = fileChannel.read(buf);
            while (bytesRead != -1) {
                buf.flip();
                while (buf.hasRemaining()) {
                    char c = (char) buf.get();
                    System.out.print(c);
                    indexContent.append(c);
                }
                buf.compact();
                bytesRead = fileChannel.read(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        CreateIndexRequest request = new CreateIndexRequest(getIndex());
        request.source(indexContent.toString(), XContentType.JSON);
        try {
            CreateIndexResponse createIndexResponse = getRestHighLevelClient().indices().create(request, RequestOptions.DEFAULT);
            boolean acknowledged = createIndexResponse.isAcknowledged();
            boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
        } catch (IOException e) {
            //todo
            e.printStackTrace();
        }
    }

    public boolean add(T object) {
        IndexRequest indexRequest = new IndexRequest(getIndex());
        indexRequest.source(JSON.toJSONString(object), XContentType.JSON);
        IndexResponse indexResponse = null;
        try {
            indexResponse = getRestHighLevelClient().index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }

        if (RestStatus.OK == indexResponse.status()) {
            return true;
        }
        return false;
    }


    public boolean bulkAdd(List<T> list) {
        BulkRequest bulkRequest = new BulkRequest(getIndex());
        List<IndexRequest> indexRequests = list.stream().map(e -> {
            IndexRequest indexRequest = new IndexRequest(getIndex());
            indexRequest.source(JSON.toJSONString(e), XContentType.JSON);
            return indexRequest;
        }).collect(Collectors.toList());

        indexRequests.forEach(e -> bulkRequest.add(e));
        //超时2分钟
        bulkRequest.timeout(TimeValue.timeValueMinutes(2));
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = getRestHighLevelClient().bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }

        if (RestStatus.OK == bulkResponse.status()) {
            return true;
        }
        return false;
    }


    public boolean delete(String id) {
        DeleteRequest deleteRequest =
                new DeleteRequest(getIndex(), id);

        DeleteResponse response = null;
        try {
            response = getRestHighLevelClient().delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
        if (RestStatus.OK == response.status()) {
            return true;
        }
        return false;

    }


    public boolean deleteByQuery(List<QueryField> queryParam) {
        DeleteByQueryRequest deleteRequest =
                new DeleteByQueryRequest(getIndex());
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!CollectionUtils.isEmpty(queryParam)) {
            for (QueryField queryField : queryParam) {
                boolQueryBuilder.must(QueryBuilders.termQuery(queryField.getField(), queryField.getValue()));
            }
        }
        deleteRequest.setQuery(boolQueryBuilder);

        getRestHighLevelClient().deleteByQueryAsync(deleteRequest, RequestOptions.DEFAULT, new ActionListener<BulkByScrollResponse>() {

            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                Long res = bulkByScrollResponse.getDeleted();
            }

            @Override
            public void onFailure(Exception e) {
                log.warn(e.toString());
            }
        });

        return false;
    }


    /**
     * 使用forceMerge可以及时释放磁盘空间，但是会带来cpu/io消耗增加，缓存失效等问题。
     */
    public void forceMerge() {
        ForceMergeRequest request = new ForceMergeRequest(getIndex());

        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        request.maxNumSegments(1);
        request.onlyExpungeDeletes(true);
        request.flush(true);
        try {
            getRestHighLevelClient().indices().forcemergeAsync(request, RequestOptions.DEFAULT, new ActionListener<ForceMergeResponse>() {
                @Override
                public void onResponse(ForceMergeResponse forceMergeResponse) {
                    int totalShards = forceMergeResponse.getTotalShards();
                    int successfulShards = forceMergeResponse.getSuccessfulShards();
                    int failedShards = forceMergeResponse.getFailedShards();
                    DefaultShardOperationFailedException[] failures = forceMergeResponse.getShardFailures();
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        } catch (ElasticsearchException exception) {
            if (exception.status() == RestStatus.NOT_FOUND) {
                //TODO
            }
        }


        //todo
    }

    public boolean update(T object) {
        UpdateRequest request = new UpdateRequest(getIndex(), object.getId());
        request.doc(JSON.toJSONString(object), XContentType.JSON);
        UpdateResponse updateResponse = null;
        try {
            updateResponse = getRestHighLevelClient().update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }

        if (RestStatus.OK == updateResponse.status()) {
            return true;
        }
        return false;
    }

    public SearchResult scroll(List<QueryField> queryParam, List<QueryField> notParam, List<QueryField> sort, List<RangeField> rangeFieldList, String scrollId) {
        // 1 条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        packSearchCondition(boolQueryBuilder, queryParam, notParam, rangeFieldList);
        //2 排序
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (!CollectionUtils.isEmpty(sort)) {
            for (QueryField queryField : sort) {
                FieldSortBuilder sortBuilder = SortBuilders.fieldSort(queryField.getField());
                sortBuilder.order(SortOrder.DESC);
                if ("asc".equals(queryField.getValue())) {
                    sortBuilder.order(SortOrder.ASC);
                }
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        FieldSortBuilder idSort = SortBuilders.fieldSort("_id");
        idSort.order(SortOrder.DESC);

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.sort(idSort);
        searchSourceBuilder.size(10);
        SearchRequest searchRequest = null;
        SearchScrollRequest searchScrollRequest = null;
        if (!StringUtils.hasText(scrollId)) {
            searchRequest = new SearchRequest(getIndex());
            searchRequest.source(searchSourceBuilder);
        } else {
            searchScrollRequest = new SearchScrollRequest(scrollId);
            searchScrollRequest.scroll(TimeValue.timeValueMillis(1));
        }
        SearchResponse response;
        try {
            if (StringUtils.hasText(scrollId)) {
                response = getRestHighLevelClient().scroll(searchScrollRequest, RequestOptions.DEFAULT);
            } else {
                response = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
            }
            RestStatus status = response.status();
            if (status != RestStatus.OK) {
                log.warn("搜索错误:{}", status.getStatus());
                return SearchResult.error("搜索错误");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return SearchResult.error("搜索错误");
        }
        SearchHits hits = response.getHits();
        //得到总数
        Long total = hits.getTotalHits().value;
        if (total <= 0) {
            return SearchResult.error("无数据");
        }
        SearchHit[] searchHits = hits.getHits();
        if (searchHits.length <= 0) {
            return SearchResult.error("数据到底了");
        }
        List<T> list = new ArrayList<>(total.intValue());

        for (SearchHit hit : searchHits) {
            T object =
                    (T) JSONObject.parseObject(hit.getSourceAsString(), getClassType());
            object.setId(hit.getId());
            list.add(object);
        }
        return SearchResult.scroll(list, response.getScrollId(), total);
    }

    public SearchResult searchAfter(List<QueryField> queryParam, List<QueryField> notParam, List<QueryField> sort, List<RangeField> rangeFieldList, Object[] searchAfter) {
        // 1 条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        packSearchCondition(boolQueryBuilder, queryParam, notParam, rangeFieldList);

        //2 排序
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (!CollectionUtils.isEmpty(sort)) {
            for (QueryField queryField : sort) {
                FieldSortBuilder sortBuilder = SortBuilders.fieldSort(queryField.getField());
                if ("asc".equals(queryField.getValue())) {
                    sortBuilder.order(SortOrder.ASC);
                }
                if ("desc".equals(queryField.getValue())) {
                    sortBuilder.order(SortOrder.DESC);
                }
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        FieldSortBuilder idSort = SortBuilders.fieldSort("_id");
        idSort.order(SortOrder.DESC);

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.sort(idSort);

        searchSourceBuilder.size(10);
        if (null != searchAfter && searchAfter.length > 0) {
            searchSourceBuilder.searchAfter(searchAfter);
        }

        SearchRequest searchRequest = new SearchRequest(getIndex());
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;
        try {
            response = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
            RestStatus status = response.status();
            if (status != RestStatus.OK) {
                log.warn("搜索错误{}", status.getStatus());
                return SearchResult.error("搜索错误");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return SearchResult.error("搜索错误");
        }
        SearchHits hits = response.getHits();
        //得到总数
        Long total = hits.getTotalHits().value;
        if (total <= 0) {
            return SearchResult.error("无数据");
        }
        SearchHit[] searchHits = hits.getHits();
        if (searchHits.length <= 0) {
            return SearchResult.error("数据到底了");
        }
        List<T> list = new ArrayList<>(total.intValue());

        for (SearchHit hit : searchHits) {
            T object =
                    (T) JSONObject.parseObject(hit.getSourceAsString(), getClassType());
            object.setId(hit.getId());
            list.add(object);
        }
        try {
            Object[] search = searchHits[searchHits.length - 1].getSortValues();
            return SearchResult.searchAfter(list, search, total);
        } catch (Exception e) {
            return SearchResult.error("搜索错误");
        }
    }


    public Map<Object, Object> termAggregation(List<QueryField> queryParam, List<QueryField> notParam, List<RangeField> rangeFieldList, String x, String y, String calculateType, String XType, Integer size) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        packSearchCondition(boolQueryBuilder, queryParam, notParam, rangeFieldList);

        //3 聚合  x轴
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("XName")
                .field(x);
        // 二级求和 对应y轴
        subAggregation(termsAggregationBuilder, calculateType, y);
        //结果降序
        if (!CalculateType.TOP_HITS.getValue().equals(calculateType)) {
            termsAggregationBuilder.order(BucketOrder.aggregation("res", false));
        }
        termsAggregationBuilder.collectMode(Aggregator.SubAggCollectionMode.BREADTH_FIRST);

        termsAggregationBuilder.size(size);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        SearchRequest searchRequest = new SearchRequest(getIndex());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse;

        try {
            searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new HashMap<>(0);
        }
        long total = searchResponse.getHits().getTotalHits().value;
        if (total <= 0) {
            return new HashMap<>(0);
        }
        Map<Object, Object> map = new LinkedHashMap<>();
        Aggregations aggregations = searchResponse.getAggregations();

        ParsedTerms terms = null;
        if (EsConstants.KEYWORD.equals(XType)) {
            terms = (ParsedStringTerms) aggregations.getAsMap().get("XName");
        }
        if (EsConstants.INTEGER.equals(XType) || EsConstants.DATE.equals(XType)) {
            terms = (ParsedLongTerms) aggregations.getAsMap().get("XName");
        }
        if (EsConstants.DOUBLE.equals(XType)) {
            terms = (ParsedDoubleTerms) aggregations.getAsMap().get("XName");
        }
        if (EsConstants.LONG.equals(XType)) {
            terms = (ParsedLongTerms) aggregations.getAsMap().get("XName");
        }

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String name = bucket.getKeyAsString();
            //解析
            if (CalculateType.TOP_HITS.getValue().equals(calculateType)) {
                List<T> res = new ArrayList<>();
                ParsedTopHits parsedTopHits = bucket.getAggregations().get("res");
                SearchHit[] searchHits = parsedTopHits.getHits().getHits();
                for (SearchHit hit : searchHits) {
                    T object =
                            (T) JSONObject.parseObject(hit.getSourceAsString(), getClassType());
                    object.setId(hit.getId());
                    res.add(object);
                }
                map.put(name, res);
            } else {
                ParsedSum.SingleValue parsedSum = bucket.getAggregations().get("res");
                String res = parsedSum.getValueAsString();
                map.put(name, res);
            }
        }
        return map;
    }


    public Map<Object, Object> dateHistogram(List<QueryField> queryParam, List<QueryField> notParam, List<RangeField> rangeFieldList, String x, String y, String calculateType, String interval) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        packSearchCondition(boolQueryBuilder, queryParam, notParam, rangeFieldList);

        //3 聚合  x轴
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("XName")
                .field(x);
        //间隔
        if ("YEAR".equals(interval)) {
            dateHistogramAggregationBuilder.calendarInterval(DateHistogramInterval.YEAR);
        }
        if ("MONTH".equals(interval)) {
            dateHistogramAggregationBuilder.calendarInterval(DateHistogramInterval.MONTH);
        }
        if ("DAY".equals(interval)) {
            dateHistogramAggregationBuilder.calendarInterval(DateHistogramInterval.DAY);
        }
        if ("HOUR".equals(interval)) {
            dateHistogramAggregationBuilder.calendarInterval(DateHistogramInterval.HOUR);
        }

        // 二级求和 对应y轴
        subAggregation(dateHistogramAggregationBuilder, calculateType, y);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);

        SearchRequest searchRequest = new SearchRequest(getIndex());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse;
        try {
            searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new HashMap<>(0);
        }
        long total = searchResponse.getHits().getTotalHits().value;
        if (total <= 0) {
            return new HashMap<>(0);
        }
        Map<Object, Object> map = new LinkedHashMap<>();
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedDateHistogram parsedDateHistogram = (ParsedDateHistogram) aggregations.getAsMap().get("XName");
        List<? extends Histogram.Bucket> buckets = parsedDateHistogram.getBuckets();
        for (Histogram.Bucket bucket : buckets) {
            String name = bucket.getKeyAsString();
            //解析
            if (CalculateType.TOP_HITS.getValue().equals(calculateType)) {
                List<T> res = new ArrayList<>();
                ParsedTopHits parsedTopHits = bucket.getAggregations().get("res");
                SearchHit[] searchHits = parsedTopHits.getHits().getHits();
                for (SearchHit hit : searchHits) {
                    T object =
                            (T) JSONObject.parseObject(hit.getSourceAsString(), getClassType());
                    object.setId(hit.getId());
                    res.add(object);
                }
                map.put(name, res);
            } else {
                ParsedSum.SingleValue parsedSum = bucket.getAggregations().get("res");
                String res = parsedSum.getValueAsString();
                map.put(name, res);
            }

        }
        return map;
    }


    public Map<Object, Object> histogram(List<QueryField> queryParam, List<QueryField> notParam, List<RangeField> rangeFieldList, String x, String y, String calculateType, double interval) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        packSearchCondition(boolQueryBuilder, queryParam, notParam, rangeFieldList);

        //3 聚合  x轴
        HistogramAggregationBuilder histogramAggregationBuilder = AggregationBuilders.histogram("XName")
                .field(x);
        // 二级求和 对应y轴
        subAggregation(histogramAggregationBuilder, calculateType, y);

        //设置间隔
        histogramAggregationBuilder.interval(interval);


        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(histogramAggregationBuilder);

        SearchRequest searchRequest = new SearchRequest(getIndex());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse;
        try {
            searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new HashMap<>(0);
        }
        long total = searchResponse.getHits().getTotalHits().value;
        if (total <= 0) {
            return new HashMap<>(0);
        }
        Map<Object, Object> map = new LinkedHashMap<>();
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedHistogram parsedHistogram = (ParsedHistogram) aggregations.getAsMap().get("XName");
        List<? extends Histogram.Bucket> buckets = parsedHistogram.getBuckets();
        for (Histogram.Bucket bucket : buckets) {
            String name = bucket.getKeyAsString();
            //对应解析二级求和
            if (CalculateType.TOP_HITS.getValue().equals(calculateType)) {
                List<T> res = new ArrayList<>();
                ParsedTopHits parsedTopHits = bucket.getAggregations().get("res");
                SearchHit[] searchHits = parsedTopHits.getHits().getHits();
                for (SearchHit hit : searchHits) {
                    T object =
                            (T) JSONObject.parseObject(hit.getSourceAsString(), getClassType());
                    object.setId(hit.getId());
                    res.add(object);
                }
                map.put(name, res);
            } else {
                ParsedSum.SingleValue parsedSum = bucket.getAggregations().get("res");
                String res = parsedSum.getValueAsString();
                map.put(name, res);
            }
        }
        return map;
    }

    public Map<Object, Object> dateRange(List<QueryField> queryParam, List<QueryField> notParam, List<RangeField> rangeFieldList, String x, String y, String calculateType, double from, double to) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        packSearchCondition(boolQueryBuilder, queryParam, notParam, rangeFieldList);

        //3 聚合  x轴
        DateRangeAggregationBuilder dateRangeAggregationBuilder = AggregationBuilders.dateRange("XName")
                .field(x);
        // 二级求和 对应y轴
        subAggregation(dateRangeAggregationBuilder, calculateType, y);
        //todo 支持多个range


        dateRangeAggregationBuilder.addRange(from, to);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(dateRangeAggregationBuilder);

        SearchRequest searchRequest = new SearchRequest(getIndex());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse;
        try {
            searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new HashMap<>(0);
        }
        long total = searchResponse.getHits().getTotalHits().value;
        if (total <= 0) {
            return new HashMap<>(0);
        }
        Map<Object, Object> map = new LinkedHashMap<>();
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedDateRange parsedDateRange = (ParsedDateRange) aggregations.getAsMap().get("XName");
        List<? extends Range.Bucket> buckets = parsedDateRange.getBuckets();
        for (Range.Bucket bucket : buckets) {
            String name = bucket.getKeyAsString();
            //对应解析二级求和
            if (CalculateType.TOP_HITS.getValue().equals(calculateType)) {
                List<T> res = new ArrayList<>();
                ParsedTopHits parsedTopHits = bucket.getAggregations().get("res");
                SearchHit[] searchHits = parsedTopHits.getHits().getHits();
                for (SearchHit hit : searchHits) {
                    T object =
                            (T) JSONObject.parseObject(hit.getSourceAsString(), getClassType());
                    object.setId(hit.getId());
                    res.add(object);
                }
                map.put(name, res);
            } else {
                ParsedSum.SingleValue parsedSum = bucket.getAggregations().get("res");
                String res = parsedSum.getValueAsString();
                map.put(name, res);
            }
        }
        return map;
    }


    public Map<Object, Object> nestedAggregation(List<QueryField> queryParam, List<QueryField> notParam, List<RangeField> rangeFieldList, String x, String y, String calculateType, String interval) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        packSearchCondition(boolQueryBuilder, queryParam, notParam, rangeFieldList);

        //3 聚合  x轴
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("XName")
                .field(x);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("XName1")
                .field("二级聚合嵌套字段");
        //间隔
        if ("YEAR".equals(interval)) {
            dateHistogramAggregationBuilder.calendarInterval(DateHistogramInterval.YEAR);
        }
        if ("MONTH".equals(interval)) {
            dateHistogramAggregationBuilder.calendarInterval(DateHistogramInterval.MONTH);
        }
        if ("DAY".equals(interval)) {
            dateHistogramAggregationBuilder.calendarInterval(DateHistogramInterval.DAY);
        }
        if ("HOUR".equals(interval)) {
            dateHistogramAggregationBuilder.calendarInterval(DateHistogramInterval.HOUR);
        }

        // 二级求和 对应y轴
        subAggregation(termsAggregationBuilder, calculateType, y);
        dateHistogramAggregationBuilder.subAggregation(termsAggregationBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);

        SearchRequest searchRequest = new SearchRequest(getIndex());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse;
        try {
            searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new HashMap<>(0);
        }
        long total = searchResponse.getHits().getTotalHits().value;
        if (total <= 0) {
            return new HashMap<>(0);
        }
        Map<Object, Object> map = new LinkedHashMap<>();
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedDateHistogram parsedDateHistogram = (ParsedDateHistogram) aggregations.getAsMap().get("XName");
        List<? extends Histogram.Bucket> buckets = parsedDateHistogram.getBuckets();
        for (Histogram.Bucket bucket : buckets) {
            String name = bucket.getKeyAsString();
            //解析
            if (CalculateType.TOP_HITS.getValue().equals(calculateType)) {
                List<T> res = new ArrayList<>();
                ParsedTopHits parsedTopHits = bucket.getAggregations().get("res");
                SearchHit[] searchHits = parsedTopHits.getHits().getHits();
                for (SearchHit hit : searchHits) {
                    T object =
                            (T) JSONObject.parseObject(hit.getSourceAsString(), getClassType());
                    object.setId(hit.getId());
                    res.add(object);
                }
                map.put(name, res);
            } else {
                ParsedSum.SingleValue parsedSum = bucket.getAggregations().get("res");
                String res = parsedSum.getValueAsString();
                map.put(name, res);
            }

        }
        return map;
    }


    /**
     * 包装搜索条件
     *
     * @param boolQueryBuilder
     * @param queryParam
     * @param notParam
     * @param rangeFieldList
     */
    protected void packSearchCondition(BoolQueryBuilder boolQueryBuilder, List<QueryField> queryParam, List<QueryField> notParam, List<RangeField> rangeFieldList) {
        //1 条件
        if (!CollectionUtils.isEmpty(queryParam)) {
            for (QueryField queryField : queryParam) {
                boolQueryBuilder.must(QueryBuilders.termQuery(queryField.getField(), queryField.getValue()));

            }
        }
        if (!CollectionUtils.isEmpty(notParam)) {
            for (QueryField queryField : notParam) {
                boolQueryBuilder.mustNot(QueryBuilders.termQuery(queryField.getField(), queryField.getValue()));
            }
        }
        //2 范围
        if (!CollectionUtils.isEmpty(rangeFieldList)) {
            for (RangeField rangeField : rangeFieldList) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(rangeField.getField());
                rangeQueryBuilder.gte(rangeField.getGe());
                rangeQueryBuilder.lte(rangeField.getLe());
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
        }
    }

    /**
     * @return
     */

    protected void subAggregation(AggregationBuilder aggregationBuilder, String calculateType, String y) {
        if (CalculateType.SUM.getValue().equals(calculateType)) {
            aggregationBuilder.subAggregation(AggregationBuilders.sum("res").field(y));
        }
        if (CalculateType.COUNT.getValue().equals(calculateType)) {
            aggregationBuilder.subAggregation(AggregationBuilders.count("res").field(y));

        }
        if (CalculateType.AVG.getValue().equals(calculateType)) {
            aggregationBuilder.subAggregation(AggregationBuilders.avg("res").field(y));
        }
        if (CalculateType.MAX.getValue().equals(calculateType)) {
            aggregationBuilder.subAggregation(AggregationBuilders.max("res").field(y));
        }
        if (CalculateType.MIN.getValue().equals(calculateType)) {
            aggregationBuilder.subAggregation(AggregationBuilders.min("res").field(y));
        }
        if (CalculateType.TOP_HITS.getValue().equals(calculateType)) {
            aggregationBuilder.subAggregation(AggregationBuilders.topHits("res").sort(y, SortOrder.DESC).size(1));
        }
    }


    protected String getIndex() {
        Class clazz = this.getClass();
        Index index = (Index) clazz.getAnnotation(Index.class);
        String indexName = index.indexName();
        return indexName;
    }

    protected RestHighLevelClient getRestHighLevelClient() {
        return SpringUtil.getBean(RestHighLevelClient.class);
    }

    protected Class getClassType() {
        return (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

}
