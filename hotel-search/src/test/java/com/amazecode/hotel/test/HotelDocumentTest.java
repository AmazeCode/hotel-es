package com.amazecode.hotel.test;

import cn.hutool.json.JSONUtil;
import com.amazecode.hotel.pojo.Hotel;
import com.amazecode.hotel.pojo.HotelDoc;
import com.amazecode.hotel.service.IHotelService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @description: 酒店文档操作
 * @author: AmazeCode
 * @date: 2023/11/19 11:01
 */
@SpringBootTest
public class HotelDocumentTest {

    @Resource
    private IHotelService hotelService;

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
     * @description: 添加酒店数据到索引库
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 11:06
     */
    @Test
    void testAddDocument() throws IOException {
        // 根据id查询酒店数据
        Hotel hotel = hotelService.getById(36934l);
        // 转换为文档类型
        HotelDoc hotelDoc = new HotelDoc(hotel);

        // 1、准备request对象
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
        // 2、准备JSON文档
        request.source(JSONUtil.toJsonStr(hotelDoc),XContentType.JSON);
        // 3、发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    /**
     * @param
     * @description: 根据Id查询文档
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 11:32
     */
    @Test
    void testGetDocumentById() throws IOException {
        // 1、创建request对象
        GetRequest request = new GetRequest("hotel", "36934");
        // 2、发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        // 3、解析结果
        String json = response.getSourceAsString();

        HotelDoc hotelDoc = JSONUtil.toBean(json,HotelDoc.class);
        System.out.println(hotelDoc);
    }

    /**
     * @description: 修改文档
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 11:46
     */
    @Test
    void testUpdateDocument() throws IOException {

        // 1、创建对象
        UpdateRequest request = new UpdateRequest("hotel","36934");

        // 2、准备参数,每个参数为一对 key value
        request.doc("price","900","starName","四钻");
        // 3、发送请求
        client.update(request, RequestOptions.DEFAULT);
    }

    /**
     * @param
     * @description: 根据id删除文档
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 11:31
     */
    @Test
    void testDeleteDocumentById() throws IOException {
        // 1、创建request对象
        DeleteRequest request = new DeleteRequest("hotel", "36934");
        // 2、发送请求
        client.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * @description: 批量导入数据
     * @param
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 12:07
     */
    @Test
    void testBulkRequest() throws IOException {
        // 批量查询酒店数据
        List<Hotel> hotels = hotelService.list();

        // 1、创建Request
        BulkRequest request = new BulkRequest();

        // 2、准备参数，添加多个新增的Request
        for (Hotel hotel : hotels) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            // 创建新增文档的Request对象
            request.add(new IndexRequest("hotel").id(hotelDoc
                    .getId().toString())
                    .source(JSONUtil.toJsonStr(hotelDoc),XContentType.JSON));
        }
        // 3、发送请求
        client.bulk(request,RequestOptions.DEFAULT);
    }
}
