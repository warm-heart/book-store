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
        //页码
        sourceBuilder.from(0);
        // 获取记录数，默认10
        sourceBuilder.size(200);


        // 第一个是获取字段，第二个是过滤的字段，默认获取全部
        //sourceBuilder.fetchSource(new String[]{"bookId", "bookDescription","bookName"}, new String[]{});


        //构建SearchRequest
        SearchRequest searchRequest = new SearchRequest(EsConsts.INDEX_NAME);
        searchRequest.types(EsConsts.TYPE);

        searchRequest.source(sourceBuilder);
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        SearchHits hits = response.getHits();
        //得到总数
        SearchHit[] searchHits = hits.getHits();
        List<String> bookIndexTemplates = new ArrayList<>(100);
        for (SearchHit hit : searchHits) {

            BookIndexTemplate bookIndexTemplate =
                    JSONObject.parseObject(hit.getSourceAsString(), BookIndexTemplate.class);
            bookIndexTemplates.add(bookIndexTemplate.getBookId());
        }
        return bookIndexTemplates;
    }
}


