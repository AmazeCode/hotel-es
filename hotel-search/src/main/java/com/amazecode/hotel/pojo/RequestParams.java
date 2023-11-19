package com.amazecode.hotel.pojo;

import lombok.Data;

@Data
public class RequestParams {
    /*
     * 搜索关键字
     */
    private String key;
    /*
     * 页码
     */
    private Integer page;
    /*
     * 页面大小
     */
    private Integer size;

    /*
     * 排序字段
     */
    private String sortBy;

    /*
     * 城市
     */
    private String city;

    /*
     * 品牌
     */
    private String brand;

    /*
     * 星级
     */
    private String starName;

    /*
     * 最小价格
     */
    private Integer minPrice;

    /*
     * 最大价格
     */
    private Integer maxPrice;

    /*
     * 地理位置
     */
    private String location;
}
