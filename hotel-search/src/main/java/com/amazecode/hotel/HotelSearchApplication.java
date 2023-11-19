package com.amazecode.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@MapperScan("com.amazecode.hotel.mapper")
@SpringBootApplication
public class HotelSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelSearchApplication.class, args);
    }

    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.0.103:9200")
        ));
    }
}
