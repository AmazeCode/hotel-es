package com.amazecode.hotel;


import cn.hutool.json.JSONUtil;
import com.amazecode.hotel.pojo.Hotel;
import com.amazecode.hotel.pojo.HotelDoc;
import com.amazecode.hotel.service.IHotelService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class HotelDocumentTest {

    private RestHighLevelClient client;

    @Autowired
    private IHotelService hotelService;

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
     * 导入酒店数据到ES
     */
    @Test
    void addDocument() throws IOException {
        //查询酒店数据
        Hotel hotel = hotelService.getById(61083L);
        HotelDoc hotelDoc = new HotelDoc(hotel);
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
        request.source(JSONUtil.toJsonStr(hotelDoc), XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 查询文档
     */
    @Test
    void getDocument() throws IOException {
        //查询酒店数据
        GetRequest request = new GetRequest("hotel", "61083");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        HotelDoc hotelDoc = JSONUtil.toBean(response.getSourceAsString(), HotelDoc.class);
        System.out.println(hotelDoc);
    }

    /**
     * 更新文档
     */
    @Test
    void updateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("hotel", "61083");
        request.doc(XContentType.JSON, "price", 1000, "starName", "五星级");
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除文档
     */
    @Test
    void deleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("hotel", "61083");
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * 批量插入文档
     */
    @Test
    void addBulk() throws IOException {
        //查询数据库数据
        List<Hotel> hotelList = hotelService.list();
        BulkRequest request = new BulkRequest();
        for (Hotel hotel : hotelList) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            IndexRequest indexRequest = new IndexRequest("hotel").id(hotelDoc.getId().toString());
            indexRequest.source(JSONUtil.toJsonStr(hotelDoc), XContentType.JSON);
            request.add(indexRequest);
        }
        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
    }
}
