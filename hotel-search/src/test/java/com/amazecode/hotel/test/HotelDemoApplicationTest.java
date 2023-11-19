package com.amazecode.hotel.test;

import com.amazecode.hotel.service.IHotelService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @description: 业务测试类
 * @author: AmazeCode
 * @date: 2023/11/19 19:43
 */
@SpringBootTest
public class HotelDemoApplicationTest {

    @Resource
    private IHotelService hotelService;

    @Test
    void contextLoads() {
        Map<String, List<String>> filters = hotelService.filters(null);
        System.out.println(filters);
    }
}
