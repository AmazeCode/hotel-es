package com.amazecode.hotel.controller;

import com.amazecode.hotel.pojo.PageResult;
import com.amazecode.hotel.pojo.RequestParams;
import com.amazecode.hotel.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Autowired
    private IHotelService hotelService;

    /**
     * 自动补全
     */
    @GetMapping("/suggestion")
    public List<String> suggestion(@RequestParam("key") String prefix) {
        return hotelService.getSuggestion(prefix);
    }

    /**
     * 获取筛选条件(聚合,查询条件和查询酒店相同,避免聚合范围偏差)
     */
    @PostMapping("/filters")
    public Map<String, List<String>> filters(@RequestBody RequestParams requestParams) {
        return hotelService.filters(requestParams);
    }

    /**
     * @description: 查询酒店
     * @param requestParams
     * @return: com.amazecode.hotel.pojo.PageResult
     * @author: AmazeCode
     * @date: 2023/11/19 19:52
     */
    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams requestParams) {
        return hotelService.search(requestParams);
    }
}
