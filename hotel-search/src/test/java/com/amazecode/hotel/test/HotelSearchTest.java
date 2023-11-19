package com.amazecode.hotel.test;

import cn.hutool.json.JSONUtil;
import com.amazecode.hotel.pojo.Hotel;
import com.amazecode.hotel.pojo.HotelDoc;
import com.amazecode.hotel.service.IHotelService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.http.HttpHost;
import org.apache.lucene.search.BooleanQuery;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @description: 搜索操作
 * @author: AmazeCode
 * @date: 2023/11/19 11:01
 */
@SpringBootTest
public class HotelSearchTest {

    private RestHighLevelClient client;

    /**
     * @param
     * @description: 客户端初始化
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 10:21
     */
    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.0.103:9200")
        ));
    }

    /**
     * @param
     * @description: 客户端销毁
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 10:22
     */
    @AfterEach
    void afterAll() throws IOException {
        this.client.close();
    }

    /**
     * @description: 查询文档 match_all
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 15:55
     */
    @Test
    void testMatchAll() throws IOException {
        // 1、准备request
        SearchRequest request = new SearchRequest("hotel");
        // 2、准备DSL
        request.source().query(QueryBuilders.matchAllQuery());

        // 3、发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4、解析响应
        handleResponse(response);
    }

    /**
     * @description: 查询文档 match
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 15:55
     */
    @Test
    void testMatch() throws IOException {
        // 1、准备request
        SearchRequest request = new SearchRequest("hotel");
        // 2、准备DSL
        request.source().query(QueryBuilders.matchQuery("all","如家"));

        // 3、发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4、解析响应
        handleResponse(response);
    }

    /**
     * @description: 查询文档 term
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 15:55
     */
    @Test
    void testTerm() throws IOException {
        // 1、准备request
        SearchRequest request = new SearchRequest("hotel");
        // 2、准备DSL
        request.source().query(QueryBuilders.termQuery("city","上海"));
        request.source().query(QueryBuilders.rangeQuery("price").gte(100).lte(300));
        // 3、发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4、解析响应
        handleResponse(response);
    }

    /**
     * @description: 查询文档 bool
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 15:55
     */
    @Test
    void testBool() throws IOException {
        // 1、准备request
        SearchRequest request = new SearchRequest("hotel");
        // 2、准备DSL
        // 2.1、准备BoolQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 2.2、添加term
        boolQuery.must(QueryBuilders.termQuery("city","上海"));
        // 2.3、添加range
        boolQuery.filter(QueryBuilders.rangeQuery("price").gte(300).lte(3000));
        request.source().query(boolQuery);
        // 3、发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4、解析响应
        handleResponse(response);
    }

    /**
     * @description: 查询文档 page and sorts
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 15:55
     */
    @Test
    void testPageAndSort() throws IOException {
        // 页码，每页大小
        int page = 1, size = 5;
        // 1、准备request
        SearchRequest request = new SearchRequest("hotel");
        // 2、准备DSL
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().from((page-1) * size).size(size);
        request.source().sort("price", SortOrder.DESC);
        // 3、发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4、解析响应
        handleResponse(response);
    }

    /**
     * @description: 查询文档 高亮
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 15:55
     */
    @Test
    void testHighlight() throws IOException {
        // 1、准备request
        SearchRequest request = new SearchRequest("hotel");
        // 2、准备DSL
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));

        // 3、发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4、解析响应
        handleResponse(response);
    }

    /**
     * @description: 聚合查询
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 19:17
     */
    @Test
    void testAggregation() throws IOException {
        // 1、准备request
        SearchRequest request = new SearchRequest("hotel");
        // 2、准备DSL
        // 2、1设置size
        request.source().size(0);
        // 2、2聚合
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(10)
        );
        // 3、发出请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4、解析结果
        Aggregations aggregations = response.getAggregations();
        // 4.1 根据聚合名称获取聚合结果
        Terms brandTerms = aggregations.get("brandAgg");
        // 4.2 获取buckets
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        // 4.3 遍历
        for (Terms.Bucket bucket : buckets) {
            // 4.4 获取key
            String key = bucket.getKeyAsString();
            System.out.println(key);
        }

    }

    /**
     * @description: 酒店自动补全测试
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 21:35
     */
    @Test
    void testSuggest() throws IOException {
        // 1 准备request
        SearchRequest request = new SearchRequest("hotel");
        // 2 准备DSL
        request.source().suggest(new SuggestBuilder().addSuggestion(
                "suggestions",
                SuggestBuilders.completionSuggestion("suggestion")
                        .prefix("hz")
                        // 跳过重复
                        .skipDuplicates(true)
                        .size(10)
        ));

        // 3 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4 解析结果
        Suggest suggest = response.getSuggest();
        // 4.1 根据补全查询名称，获取补全结果
        CompletionSuggestion suggestion = suggest.getSuggestion("suggestions");
        // 4.2 获取options
        List<CompletionSuggestion.Entry.Option> options = suggestion.getOptions();
        // 4.3 遍历
        for (CompletionSuggestion.Entry.Option option : options) {
            String text = option.getText().toString();
            System.out.println(text);
        }
    }
    /**
     * @description: 响应解析公共方法
     * @param response
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 16:00
     */
    private static void handleResponse(SearchResponse response) {
        // 4、解析响应
        SearchHits searchHits = response.getHits();
        // 4.1、获取总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("共搜索到" + total + "条数据");
        // 4.2、文档数组
        SearchHit[] hits = searchHits.getHits();
        // 4.3 遍历
        for (SearchHit hit : hits) {
            // 获取文档source
            String json = hit.getSourceAsString();
            // 反序列化
            HotelDoc hotelDoc = JSONUtil.toBean(json, HotelDoc.class);

            // 获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();

            if (CollectionUtils.isNotEmpty(highlightFields)) {
                // 根据字段名获取高亮结果
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField != null) {
                    String name = highlightField.getFragments()[0].string();
                    // 覆盖非高亮结果
                    hotelDoc.setName(name);
                }
            }
            System.out.println("hotelDoc = " + hotelDoc);
        }
    }
}
