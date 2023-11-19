package com.amazecode.hotel.web;


import com.amazecode.hotel.constants.MqConstants;
import com.amazecode.hotel.pojo.Hotel;
import com.amazecode.hotel.pojo.PageResult;
import com.amazecode.hotel.service.IHotelService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;

@RestController
@RequestMapping("hotel")
public class HotelController {

    @Autowired
    private IHotelService hotelService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @description: 根据id查询
     * @param id
     * @return: com.amazecode.hotel.pojo.Hotel
     * @author: AmazeCode
     * @date: 2023/11/19 21:47
     */
    @GetMapping("/{id}")
    public Hotel queryById(@PathVariable("id") Long id) {
        return hotelService.getById(id);
    }

    /**
     * @description: 查询酒店数据
     * @param page
     * @param size
     * @return: com.amazecode.hotel.pojo.PageResult
     * @author: AmazeCode
     * @date: 2023/11/19 21:47
     */
    @GetMapping("/list")
    public PageResult hotelList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "1") Integer size
    ) {
        Page<Hotel> result = hotelService.page(new Page<>(page, size));

        return new PageResult(result.getTotal(), result.getRecords());
    }

    /**
     * @description: 新增酒店数据
     * @param hotel
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 21:47
     */
    @PostMapping
    public void saveHotel(@RequestBody Hotel hotel) {
        hotelService.save(hotel);
        //发送消息，提醒消费者更新ES中的数据,发送消息的消息体尽量小一点,避免队列被占满
        rabbitTemplate.convertAndSend(MqConstants.HOTEL_EXCHANGE, MqConstants.HOTEL_INSERT_KEY, hotel.getId());
    }

    /**
     * @description: 修改酒店数据
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 21:47
     */
    @PutMapping()
    public void updateById(@RequestBody Hotel hotel) {
        if (hotel.getId() == null) {
            throw new InvalidParameterException("id不能为空");
        }
        hotelService.updateById(hotel);
        //发送消息，提醒消费者更新ES中的数据,发送消息的消息体尽量小一点,避免队列被占满
        rabbitTemplate.convertAndSend(MqConstants.HOTEL_EXCHANGE, MqConstants.HOTEL_INSERT_KEY, hotel.getId());
    }

    /**
     * @description: 删除酒店数据
     * @param id
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/19 21:47
     */
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        hotelService.removeById(id);
        //发送消息，提醒消费者删除ES中的数据,发送消息的消息体尽量小一点,避免队列被占满
        rabbitTemplate.convertAndSend(MqConstants.HOTEL_EXCHANGE, MqConstants.HOTEL_DELETE_KEY, id);
    }
}
