package com.amazecode.hotel;


import cn.hutool.json.JSONUtil;
import com.amazecode.hotel.pojo.HotelDoc;
import com.amazecode.hotel.service.IHotelService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
public class HotelSearchTest {

    private RestHighLevelClient client;

    @Autowired
    private IHotelService hotelService;

    @BeforeEach
    void create() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.0.103:9200")
        ));
    }

    @AfterEach
    void close() throws Exception {
        client.close();
    }

    @Test
    void test() throws IOException {
        System.out.println(client);
    }

    /**
     * 查询所有文档
     */
    @Test
    void matchAll() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        // 查询所有
        request.source().query(QueryBuilders.matchAllQuery());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        System.out.println("总记录数：" + total);
        hits.forEach(hit -> {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSONUtil.toBean(json, HotelDoc.class);
            System.out.println(hotelDoc);
        });
    }

    /**
     * 匹配查询
     */
    @Test
    void match() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        //单字段查询
        //request.source().query(QueryBuilders.matchQuery("name", "如家"));
        //多字段查询
        request.source().query(QueryBuilders.multiMatchQuery("name", "如家", "price", "1000"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        System.out.println("总记录数：" + total);
        hits.forEach(hit -> {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSONUtil.toBean(json, HotelDoc.class);
            System.out.println(hotelDoc);
        });
    }

    /**
     * 复合查询
     */
    void bool() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        //bool查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //构建条件
        boolQuery.must(QueryBuilders.matchQuery("name", "如家"));
        boolQuery.must(QueryBuilders.rangeQuery("price").gte(1000).lte(2000));
        request.source().query(boolQuery);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        System.out.println("总记录数：" + total);
        hits.forEach(hit -> {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSONUtil.toBean(json, HotelDoc.class);
            System.out.println(hotelDoc);
        });
    }

    /**
     * 分页查询
     */
    @Test
    void page() throws Exception {
        int page = 1, size = 5;
        SearchRequest request = new SearchRequest("hotel");
        //分页查询
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().from((page - 1) * size).size(size);
        request.source().sort("price", SortOrder.ASC);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        System.out.println("总记录数：" + total);
        hits.forEach(hit -> {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSONUtil.toBean(json, HotelDoc.class);
            System.out.println(hotelDoc);
        });
    }

    /**
     * 高亮查询
     */
    @Test
    void highlight() throws IOException {
        String highlightField = "name";
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        request.source().highlighter(new HighlightBuilder()
                //高亮字段
                .field(highlightField)
                //是否需要与查询字段匹配
                .requireFieldMatch(false));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        System.out.println("总记录数：" + total);
        hits.forEach(hit -> {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSONUtil.toBean(json, HotelDoc.class);
            System.out.println(hotelDoc);
            //取出高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField name = highlightFields.get(highlightField);
            hotelDoc.setName(name.getFragments()[0].string());
            System.out.println(hotelDoc);
        });
    }

    /**
     * 聚合查询
     */
    @Test
    void agg() throws Exception {
        SearchRequest request = new SearchRequest("hotel");
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(10));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        ParsedStringTerms brandAgg = aggregations.get("brandAgg");
        brandAgg.getBuckets().forEach(bucket -> {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());
        });
    }

    /**
     * 自动补全查询
     */
    @Test
    void suggest() throws Exception {
        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .suggest(new SuggestBuilder().addSuggestion(
                        "mySuggestion",
                        SuggestBuilders
                                .completionSuggestion("suggestion")
                                .prefix("hz")
                                .skipDuplicates(true)
                                .size(10)));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Suggest suggest = response.getSuggest();
        CompletionSuggestion mySuggestion = suggest.getSuggestion("mySuggestion");
        mySuggestion.getOptions().forEach(option -> {
            String text = option.getText().string();
            System.out.println(text);
        });
    }
}
