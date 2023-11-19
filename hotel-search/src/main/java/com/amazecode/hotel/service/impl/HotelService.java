package com.amazecode.hotel.service.impl;



import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.amazecode.hotel.mapper.HotelMapper;
import com.amazecode.hotel.pojo.Hotel;
import com.amazecode.hotel.pojo.HotelDoc;
import com.amazecode.hotel.pojo.PageResult;
import com.amazecode.hotel.pojo.RequestParams;
import com.amazecode.hotel.service.IHotelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: ES 服务实现
 * @author: AmazeCode
 * @date: 2023/11/18 21:34
 */
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams requestParams) {
        try {
            //1.准备request
            SearchRequest request = new SearchRequest("hotel");
            //2.准备DSL
            buildBasicQuery(requestParams, request);
            //分页
            Integer page = requestParams.getPage();
            Integer size = requestParams.getSize();
            request.source().from((page - 1) * size).size(size);
            //根据位置排序
            String location = requestParams.getLocation();
            if (location != null && location != "") {
                request.source()
                        .sort(SortBuilders
                                .geoDistanceSort("location", new GeoPoint(location))
                                .order(SortOrder.ASC)
                                .unit(DistanceUnit.KILOMETERS));
            }
            //3.发送请求，得到响应
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4.解析结果
            return handleResponse(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<String>> filters(RequestParams requestParams) {
        Map<String, List<String>> result = new HashMap<>();
        try {
            // 1、准备request
            SearchRequest request = new SearchRequest("hotel");
            // 2、准备DSL
            // 2.1 query
            buildBasicQuery(requestParams, request);
            // 2.2 设置size
            request.source().size(0);
            // 2.3 聚合
            buildAggregation(request);
            // 3 发出请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            Aggregations aggregations = response.getAggregations();

            // 4.1 获取品牌聚合结果
            List<String> brandList = getAggByName(aggregations,"brandAgg");
            result.put("brand", brandList);
            // 4.2 获取城市聚合结果
            List<String> cityList = getAggByName(aggregations, "cityAgg");
            result.put("city", cityList);
            // 4.3 获取星级聚合结果
            List<String> starNameList = getAggByName(aggregations, "starNameAgg");
            result.put("starName", starNameList);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(100));
        request.source().aggregation(AggregationBuilders
                .terms("cityAgg")
                .field("city")
                .size(100));
        request.source().aggregation(AggregationBuilders
                .terms("starNameAgg")
                .field("starName")
                .size(100));
    }

    private static List<String> getAggByName(Aggregations aggregations, String aggName) {
        ParsedStringTerms fieldAgg = aggregations.get(aggName);
        List<String> keyList = new ArrayList<>();
        fieldAgg.getBuckets().forEach(bucket -> {
            keyList.add(bucket.getKeyAsString());
            //System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());
        });
        return keyList;
    }

    @Override
    public List<String> getSuggestion(String prefix) {
        try {
            // 1 准备request
            SearchRequest request = new SearchRequest("hotel");
            // 2 准备DSL 自定义名称mySuggestions
            request.source().suggest(new SuggestBuilder().addSuggestion(
                    "mySuggestions",
                    SuggestBuilders.completionSuggestion("suggestion")
                            .prefix(prefix)
                            // 跳过重复
                            .skipDuplicates(true)
                            .size(10)
            ));

            // 3 发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 4 解析结果
            Suggest suggest = response.getSuggest();
            // 4.1 根据补全查询名称，获取补全结果
            CompletionSuggestion suggestion = suggest.getSuggestion("mySuggestions");
            // 4.2 获取options
            List<CompletionSuggestion.Entry.Option> options = suggestion.getOptions();
            // 4.3 遍历
            List<String> result = new ArrayList<>(options.size());
            for (CompletionSuggestion.Entry.Option option : options) {
                String text = option.getText().toString();
                result.add(text);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertById(Long id) {
        try {
            Hotel hotel = getById(id);
            HotelDoc hotelDoc = new HotelDoc(hotel);
            IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
            request.source(JSONUtil.toJsonStr(hotelDoc), XContentType.JSON);
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            DeleteRequest request = new DeleteRequest("hotel", id.toString());
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description: 构建基础查询
     * @param requestParams
     * @param request
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 21:41
     */
    private static void buildBasicQuery(RequestParams requestParams, SearchRequest request) {
        //复合查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //根据关键词查询
        String key = requestParams.getKey();
        if (StrUtil.isNotBlank(key)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("all", key));
        } else {
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }
        //根据城市查询
        if (StrUtil.isNotBlank(requestParams.getCity())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
        }
        //根据品牌查询
        if (StrUtil.isNotBlank(requestParams.getBrand())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
        }
        //根据星级查询
        if (StrUtil.isNotBlank(requestParams.getStarName())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("starName", requestParams.getStarName()));
        }
        //根据价格区间查询
        if (requestParams.getMinPrice() != null && requestParams.getMaxPrice() != null) {
            boolQueryBuilder
                    .filter(QueryBuilders
                            .rangeQuery("price")
                            .gte(requestParams.getMinPrice())
                            .lte(requestParams.getMaxPrice()));
        }

        //算分控制
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                // 原始查询,相关性算分的查询
                boolQueryBuilder,
                //function score 的数组
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        //其中的一个function score 元素
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                //过滤条件
                                QueryBuilders.termQuery("isAD", true),
                                //算分函数
                                ScoreFunctionBuilders.weightFactorFunction(10)
                        )
                });
        request.source().query(functionScoreQueryBuilder);

        // 排序
        if (StrUtil.isNotBlank(requestParams.getSortBy())) {
            request.source().sort(requestParams.getSortBy(),SortOrder.DESC);
        }

    }


    /**
     * @description: 处理响应
     * @param response
     * @return: com.amazecode.hotel.pojo.PageResult
     * @author: AmazeCode
     * @date: 2023/11/19 21:41
     */
    public static PageResult handleResponse(SearchResponse response) {
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        List<HotelDoc> hotelDocList = new ArrayList<>();
        for (SearchHit hit : hits.getHits()) {
            //获取文档source
            String json = hit.getSourceAsString();
            //反序列化
            HotelDoc hotelDoc = JSONUtil.toBean(json, HotelDoc.class);
            //获取高亮结果
            /*Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField != null) {
                    String name = highlightField.getFragments()[0].toString();
                    hotelDoc.setName(name);
                }
            }*/
            //获取排序值
            Object[] sortValues = hit.getSortValues();
            if (sortValues.length > 0) {
                hotelDoc.setDistance(sortValues[0]);
            }
            hotelDocList.add(hotelDoc);
        }
        return new PageResult(total, hotelDocList);
    }

}
