package com.amazecode.hotel.test;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static com.amazecode.hotel.constants.HotelConstants.MAPPING_TEMPLATE;

/**
 * @description: 酒店索引测试
 * @author: AmazeCode
 * @date: 2023/11/19 10:18
 */
@SpringBootTest
public class HotelIndexTest {

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
     * @param
     * @description: 创建索引库
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 10:31
     */
    @Test
    void testCreateHotelIndex() throws IOException {
        // 1.创建Request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        // 2.请求参数，MAPPING_TEMPLATE是静态常量字符串，内容是创建索引库的DSL语句
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        // 3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * @param
     * @description: 删除索引库
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 10:46
     */
    @Test
    void testDeleteHotelIndex() throws IOException {
        // 1、创建request对象
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        // 2、发起请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    /**
     * @param
     * @description: 索引库是否存在
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 10:47
     */
    @Test
    void testExistHotelIndex() throws IOException {

        // 1、创建request对象
        GetIndexRequest request = new GetIndexRequest("hotel");
        // 2、发起请求
        boolean exist = client.indices().exists(request, RequestOptions.DEFAULT);
        // 3、输出
        System.out.println(exist ? "索引库已经存在!" : "索引库不存在!");
    }
}
