package com.amazecode.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HotelIndexTest {

    private RestHighLevelClient client;

    @BeforeEach
    void create() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://localhost:9400")
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
     * 创建索引
     */
    @Test
    void createHotelIndex() throws IOException {
        //1. 创建request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        //2. 准备请求参数
        //request.source(MAPPING_TEMPLATE, XContentType.JSON);
        //3. 发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除索引
     */
    @Test
    void deleteHotelIndex() throws IOException {
        //创建Request对象
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        //发送请求
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }
}
