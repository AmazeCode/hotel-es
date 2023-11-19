package com.amazecode.hotel;

import com.amazecode.hotel.service.IHotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class HotelDemoApplicationTests {

    @Autowired
    private IHotelService hotelService;

    @Test
    void contextLoads() {
        Map<String, List<String>> stringListMap = hotelService.filters(null);
        System.out.println(stringListMap);
    }

}
