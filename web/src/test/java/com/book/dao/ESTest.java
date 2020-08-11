package com.book.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.book.constant.EsConsts;
import com.book.search.BookIndexTemplate;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;


import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.text.Highlighter;
import java.io.IOException;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;


/**
 * @author wangqianlong
 * @create 2019-08-07 17:28
 */
@Slf4j
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
        rangbuilder.gte(date);
        rangbuilder.lte(date1);
        boolBuilder.must(rangbuilder);


        //可以多个条件一起相当于数据库中的 must:and ;should:OR

        //BOOK_DESCRIPTION里必须有小说关键字
        // CATEGORY_NAME古典
        //其实must 和 should 不应该一起用
        boolBuilder.must(QueryBuilders.termQuery(EsConsts.BOOK_DESCRIPTION, "小说"));
        boolBuilder.should(QueryBuilders.termQuery(EsConsts.CATEGORY_NAME, "古典"));


        //和上面写法一样
        /*boolBuilder.filter(QueryBuilders.boolQuery()
                .should(QueryBuilders.termQuery(EsConsts.BOOK_DESCRIPTION, "小说"))
                .should(QueryBuilders.termQuery(EsConsts.CATEGORY_NAME, "小说"))
        );*/


        // 排序
        FieldSortBuilder fsb = SortBuilders.fieldSort("bookPrice");
        fsb.order(SortOrder.DESC);

        //高亮  //高亮字段为搜索参数里的字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(EsConsts.CATEGORY_NAME);
        highlightBuilder.field(EsConsts.BOOK_DESCRIPTION);
        highlightBuilder.preTags("<span style=\"color:red\">");  //高亮设置
        highlightBuilder.postTags("</span>");
        //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
        highlightBuilder.fragmentSize(800000); //最大高亮分片数
        highlightBuilder.numOfFragments(0); //从第一个分片获取高亮片段

        //构建SearchSourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolBuilder);
        sourceBuilder.sort(fsb);
        sourceBuilder.highlighter(highlightBuilder);
        sourceBuilder.from(0);
        // 获取记录数，默认10
        sourceBuilder.size(100);

        // 第一个是获取字段，第二个是过滤的字段，默认获取全部
        //sourceBuilder.fetchSource(new String[]{"bookId", "bookDescription","bookName"}, new String[]{});

        //构建SearchRequest
        SearchRequest searchRequest = new SearchRequest(EsConsts.INDEX_NAME);
        searchRequest.types(EsConsts.TYPE);
        searchRequest.source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<String> bookIndexTemplates = new ArrayList<>(100);
        for (SearchHit hit : searchHits) {
            //设置高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            //和上面设置的搜索关键词对应
            HighlightField categoryNameField = highlightFields.get(EsConsts.CATEGORY_NAME);
            HighlightField bookDescriptionField = highlightFields.get(EsConsts.BOOK_DESCRIPTION);
            Map<String, Object> source = hit.getSourceAsMap();
            if(categoryNameField!=null){
                Text[] fragments = categoryNameField.fragments();
                String categoryName = "";
                for (Text text : fragments) {
                    categoryName+=text;
                }
                source.put(EsConsts.CATEGORY_NAME, categoryName);
            }
            if(bookDescriptionField!=null){
                Text[] fragments = bookDescriptionField.fragments();
                String bookDescription = "";
                for (Text text : fragments) {
                    bookDescription+=text;
                }
                source.put(EsConsts.BOOK_DESCRIPTION, bookDescription);
            }
            System.err.println(source);
          //  System.err.println("search -> " + hit.getSourceAsString());
////             System.out.println(hit.getHighlightFields().get(EsConsts.CATEGORY_NAME).getFragments());
//            highlightFields.forEach((k, v) -> System.out.println("k-->" + v));
            //结果映射到实体类 es不适合做存储，适合做搜索，搜索到主键再去数据库根据ID批量查找。
            BookIndexTemplate bookIndexTemplate =
                    JSONObject.parseObject(hit.getSourceAsString(), BookIndexTemplate.class);
            bookIndexTemplates.add(bookIndexTemplate.getBookId());
        }
        //bookIndexTemplates.forEach(e -> System.out.println(e));

    }


    //深分页
    @Test
    public void deepPage() throws IOException, ParseException {


        //该scroll参数（传递到search请求和每个scroll 请求）告诉Elasticsearch应该保持多长时间的搜索上下文活着。
        // 它的值（例如1m，参见“ 时间单位”）不需要足够长的时间来处理所有数据，而只需要足够长的时间来处理前一批结果即可。
        // 每个scroll请求（带有scroll参数）都设置一个新的到期时间。如果scroll没有在scroll 参数中传递请求，
        // 那么搜索上下文将作为该 scroll 请求的一部分被释放。

        //scroll超过超时时间后，搜索上下文将自动删除。但是，如上一节所述，保持滚动打开是有代价的，
        // 因此，一旦不再使用clear-scrollAPI 使用滚动，则应明确清除滚动 ：

        //如果scroll为空 则认为是第一次查询，如果不为空则直接scroll查询
        Scroll scroll =new Scroll(TimeValue.timeValueMinutes(2));
        //构建查询条件
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

        //可以多个条件一起相当于数据库中的 must:and ;should:OR


        boolBuilder.should(QueryBuilders.termQuery(EsConsts.BOOK_DESCRIPTION, "小说"));
        boolBuilder.should(QueryBuilders.termQuery(EsConsts.CATEGORY_NAME, "古典"));

        //构建SearchSourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolBuilder);

        //设定每次返回多少条数据
        sourceBuilder.size(1);

        //构建SearchRequest
        SearchRequest searchRequest = new SearchRequest(EsConsts.INDEX_NAME);
        searchRequest.types(EsConsts.TYPE);
        searchRequest.source(sourceBuilder);
        //分页
        searchRequest.scroll(scroll);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        String scrollId = response.getScrollId();

        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            System.out.println(hit.getSourceAsString());
        }

        //遍历搜索命中的数据，直到没有数据
        while (searchHits != null && searchHits.length > 0) {

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            //每次重新设置过期时间
            scrollRequest.scroll(TimeValue.timeValueMillis(1));

            scrollRequest.scroll(scroll);
            try {
                response = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            scrollId = response.getScrollId();
            searchHits = response.getHits().getHits();
            if (searchHits != null && searchHits.length > 0) {
                System.out.println("-----下一页-----");
                for (SearchHit searchHit : searchHits) {
                    System.out.println(searchHit.getSourceAsString());
                }
            }
        }
        //清除滚屏
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);//也可以选择setScrollIds()将多个scrollId一起使用
        ClearScrollResponse clearScrollResponse = null;
        try {
            clearScrollResponse = client.clearScroll(clearScrollRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean succeeded = clearScrollResponse.isSucceeded();
        System.out.println("succeeded:" + succeeded);
    }
}
