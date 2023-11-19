package com.amazecode.hotel.constants;

/**
 * @description: MQ常量
 * @author: AmazeCode
 * @date: 2023/11/18 21:06
 */
public class MqConstants {

    /**
     * 交换机
     */
    public static final String HOTEL_EXCHANGE = "hotel.topic";

    /**
     * 监听增加和修改的队列
     */
    public static final String HOTEL_INSERT_QUEUE = "hotel.insert.queue";

    /**
     * 监听删除的队列
     */
    public static final String HOTEL_DELETE_QUEUE = "hotel.delete.queue";

    /**
     * 新增或修改的RoutingKey
     */
    public static final String HOTEL_INSERT_KEY = "hotel.insert";

    /**
     * 删除的RoutingKey
     */
    public static final String HOTEL_DELETE_KEY = "hotel.delete";
}
