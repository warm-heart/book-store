package com.book.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.book.constant.EsConsts;
import com.book.search.BookIndexTemplate;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
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
import org.elasticsearch.index.query.RangeQueryBuilder;

import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;


import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author wangqianlong
 * @create 2019-08-07 17:28
 */

public class ESTest extends StartApplicationTests {


    @Autowired
    private RestHighLevelClient client;


    @Test
    public void add() throws IOException {
        BookIndexTemplate bookIndexTemplate = new BookIndexTemplate();
        bookIndexTemplate.setBookId("6");
        bookIndexTemplate.setBookName("斗罗大陆");
        bookIndexTemplate.setBookDescription("古典的小说");
        bookIndexTemplate.setBookStock(100);
        bookIndexTemplate.setBookPrice(59.9);
        bookIndexTemplate.setCategoryName("小说");
        bookIndexTemplate.setCreateTime(new Date());

        IndexRequest indexRequest = new IndexRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, bookIndexTemplate.getBookId());
        indexRequest.source(JSON.toJSONString(bookIndexTemplate), XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        System.out.println("add: " + JSON.toJSONString(indexResponse));
    }

    @Test
    public void delete() throws IOException {
        DeleteRequest deleteRequest =
                new DeleteRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, "3");
        DeleteResponse response = client.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println("delete: " + JSON.toJSONString(response));
    }

    @Test
    public void update() throws IOException {
        BookIndexTemplate bookIndexTemplate = new BookIndexTemplate();
        bookIndexTemplate.setBookId("5");
        bookIndexTemplate.setBookName("新增加");
        bookIndexTemplate.setBookDescription("古典作品");
        bookIndexTemplate.setBookStock(100);
        bookIndexTemplate.setBookPrice(79.9);
        bookIndexTemplate.setCategoryName("小说");
        bookIndexTemplate.setCreateTime(new Date());

        UpdateRequest request = new UpdateRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, "5");
        request.doc(JSON.toJSONString(bookIndexTemplate), XContentType.JSON);
        UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
        System.out.println("update: " + JSON.toJSONString(updateResponse));
        System.out.println(updateResponse.status());
        System.out.println(RestStatus.OK);
        System.out.println(updateResponse.status().getStatus());
        System.out.println(updateResponse.status() == RestStatus.OK);


    }


    @Test
    public void get() throws IOException {
        String id = "2";
        GetRequest getRequest = new GetRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, id.toString());
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

        byte[] sourceAsBytes = getResponse.getSourceAsBytes();

        JSONObject jsonObject = new JSONObject();

        BookIndexTemplate book = JSON.parseObject(sourceAsBytes, BookIndexTemplate.class);
        System.out.println(book);
        System.out.println("get: " + jsonObject.parseObject(sourceAsBytes, BookIndexTemplate.class));


    }

    @Test
    public void search() throws IOException, ParseException {
        //构建查询条件
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

        // 查询在时间区间范围内的结果
        RangeQueryBuilder rangbuilder = QueryBuilders.rangeQuery("createTime");
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //加上时间
        Date date = sDateFormat.parse("2019-08-07 16:30:00");
        Date date1 = sDateFormat.parse("2019-09-13 12:00:00");
        System.out.println(date);
        System.out.println(date1);
        rangbuilder.gte(date);
        rangbuilder.lte(date1);
        boolBuilder.must(rangbuilder);


        //可以多个条件一起相当于数据库中的 OR
        boolBuilder.must(QueryBuilders.termQuery(EsConsts.BOOK_DESCRIPTION, "古典"));
        boolBuilder.should(QueryBuilders.termQuery(EsConsts.CATEGORY_NAME, "小说"));


        //和上面写法一样
        /*boolBuilder.filter(QueryBuilders.boolQuery()
                .should(QueryBuilders.termQuery(EsConsts.BOOK_DESCRIPTION, "小说"))
                .should(QueryBuilders.termQuery(EsConsts.CATEGORY_NAME, "小说"))
        );*/


        // 排序
        FieldSortBuilder fsb = SortBuilders.fieldSort("bookPrice");
        fsb.order(SortOrder.DESC);


        //构建SearchSourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolBuilder);
        sourceBuilder.sort(fsb);

        sourceBuilder.from(0);
        // 获取记录数，默认10
        sourceBuilder.size(100);

        // 第一个是获取字段，第二个是过滤的字段，默认获取全部
        //sourceBuilder.fetchSource(new String[]{"bookId", "bookDescription","bookName"}, new String[]{});


        //构建SearchRequest
        SearchRequest searchRequest = new SearchRequest(EsConsts.INDEX_NAME);
        searchRequest.types(EsConsts.TYPE);
        searchRequest.source(sourceBuilder);
        SearchResponse response = null;
        response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<String> bookIndexTemplates = new ArrayList<>(100);
        for (SearchHit hit : searchHits) {
            System.out.println("search -> " + hit.getSourceAsString());
            // System.out.println(hit.getHighlightFields().get("bookName").getFragments());
            BookIndexTemplate bookIndexTemplate =
                    JSONObject.parseObject(hit.getSourceAsString(), BookIndexTemplate.class);
            bookIndexTemplates.add(bookIndexTemplate.getBookId());
        }
        bookIndexTemplates.forEach(e -> System.out.println(e));

    }
}
