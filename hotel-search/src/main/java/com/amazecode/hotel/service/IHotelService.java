package com.amazecode.hotel.service;



import com.amazecode.hotel.pojo.Hotel;
import com.amazecode.hotel.pojo.PageResult;
import com.amazecode.hotel.pojo.RequestParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @description: ES 服务层
 * @author: AmazeCode
 * @date: 2023/11/18 21:34
 */
public interface IHotelService extends IService<Hotel> {

    /**
     * @description: 分页查询
     * @param requestParams 请求参数
     * @return: com.amazecode.hotel.pojo.PageResult
     * @author: AmazeCode
     * @date: 2023/11/18 21:44
     */
    PageResult search(RequestParams requestParams);

    /**
     * @description: 过滤查询(城市、品牌、星级)
     * @param requestParams 请求参数
     * @return: java.util.Map<java.lang.String,java.util.List<java.lang.String>>
     * @author: AmazeCode
     * @date: 2023/11/18 21:45
     */
    Map<String, List<String>> filters(RequestParams requestParams);

    /**
     * @description: 模糊匹配
     * @param prefix 匹配前缀
     * @return: java.util.List<java.lang.String>
     * @author: AmazeCode
     * @date: 2023/11/18 21:45
     */
    List<String> getSuggestion(String prefix);

    /**
     * @description: 新增记录
     * @param id 记录id
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/18 21:45
     */
    void insertById(Long id);

    /**
     * @description: 删除记录
     * @param id 记录id
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/18 21:45
     */
    void deleteById(Long id);
}
