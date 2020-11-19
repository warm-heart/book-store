package com.book.search;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.book.constant.EsConsts;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangqianlong
 * @create 2019-08-08 9:07
 */
@Service
@Slf4j
public class BookSearchServiceImpl implements BookSearchService {
    @Autowired
    private RestHighLevelClient client;

    @Override
    public boolean add(BookIndexTemplate bookIndexTemplate) {

        IndexRequest indexRequest = new IndexRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, bookIndexTemplate.getBookId());
        indexRequest.source(JSON.toJSONString(bookIndexTemplate), XContentType.JSON);
        IndexResponse indexResponse = null;
        try {
            indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }

        if (RestStatus.OK == indexResponse.status())
            return true;
        return false;

    }


    @Override
    public boolean remove(String bookId) {
        DeleteRequest deleteRequest =
                new DeleteRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, bookId);
        DeleteResponse response = null;
        try {
            response = client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
        if (RestStatus.OK == response.status())
            return true;
        return false;

    }

    @Override
    public boolean update(BookIndexTemplate bookIndexTemplate) {
        UpdateRequest request = new UpdateRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, bookIndexTemplate.getBookId());
        request.doc(JSON.toJSONString(bookIndexTemplate), XContentType.JSON);
        UpdateResponse updateResponse = null;
        try {
            updateResponse = client.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }

        if (RestStatus.OK == updateResponse.status())
            return true;
        return false;

    }


    @Override
    public List<String> search(String queryParam) {

        //构建查询条件
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

        //可以多个条件一起相当于数据库中的 OR
        // matchQuery会对查询参数再次分词，termQuery不会，
        boolBuilder.should(QueryBuilders.termQuery(EsConsts.BOOK_DESCRIPTION, queryParam));
        boolBuilder.should(QueryBuilders.matchQuery(EsConsts.CATEGORY_NAME, queryParam));

        //和上面写法一样
//        boolBuilder.filter(QueryBuilders.boolQuery()
//                .should(QueryBuilders.termQuery(EsConsts.BOOK_DESCRIPTION, "小说"))
//                .should(QueryBuilders.termQuery(EsConsts.CATEGORY_NAME, "小说"))
//        );

        // 排序 按照价格降序
        FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort("bookPrice");
        fieldSortBuilder.order(SortOrder.DESC);

        //构建SearchSourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //加入搜索条件
        sourceBuilder.query(boolBuilder);
        //加入排序
        sourceBuilder.sort(fieldSortBuilder);

        //分页参数前端传 这里没做特此说明   此方式深度搜索很耗时 建议深分页scroll
        //每页的起始索引 from参数是偏移量 比如每页100   第二页100-200 from 为100，size为100
        sourceBuilder.from(0);
        //每页的数量  获取记录数，默认10
        sourceBuilder.size(200);


        // 第一个是获取字段，第二个是过滤的字段，默认获取全部
        //sourceBuilder.fetchSource(new String[]{"bookId", "bookDescription","bookName"}, new String[]{});


        //构建SearchRequest
        SearchRequest searchRequest = new SearchRequest(EsConsts.INDEX_NAME);
        searchRequest.types(EsConsts.TYPE);

        searchRequest.source(sourceBuilder);
        SearchResponse response;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
            RestStatus status = response.status();
            if (status!=RestStatus.OK){
                log.warn("搜索错误{}",status.getStatus());
                return null;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        SearchHits hits = response.getHits();

        //得到总数
        long total = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        List<String> bookIndexTemplates = new ArrayList<>(100);
        for (SearchHit hit : searchHits) {

            BookIndexTemplate bookIndexTemplate =
                    JSONObject.parseObject(hit.getSourceAsString(), BookIndexTemplate.class);
            bookIndexTemplates.add(bookIndexTemplate.getBookId());
        }
        return bookIndexTemplates;
    }
    //聚合操作
      public Map<String, Long> aggregation() throws IOException {

        SearchRequest searchRequest = new SearchRequest(EsConstants.POS_BILL_ITEM_INDEX_NAME);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        // 根据产品类别id分组，对每个分组下的销售量进行sum求个

        // 这一步是对itemClassId进行聚合
        // term 后面跟的是别名，field是es中的索引字段
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("name")
                .field("itemName");

        //设置多少个分组
        termsAggregationBuilder.size(5);

        //对分组下的 itemQty（销售量） 求和操作  //这里是二级求和
        termsAggregationBuilder.subAggregation(AggregationBuilders.sum("sum").field("itemQty"));

        searchSourceBuilder.aggregation(termsAggregationBuilder);


        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        Map<String, Long> map = new LinkedHashMap<>();

        Aggregations aggregations = searchResponse.getAggregations();

        ParsedStringTerms terms = (ParsedStringTerms) aggregations.getAsMap().get("name");
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            Long count = bucket.getDocCount();
            //对应解析二级求和
            ParsedSum parsedSum = bucket.getAggregations().get("sum");
            //该分组下的销售量
            Double sum = parsedSum.getValue();
            map.put(keyAsString, count);
        }
        return map;
    }
}


