package com.amazecode.hotel.mq;

import com.amazecode.hotel.constants.MqConstants;
import com.amazecode.hotel.service.IHotelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description: MQ监听类
 * @author: AmazeCode
 * @date: 2023/11/18 21:34
 */
@Slf4j
@Component
public class HotelListener {

    @Resource
    private IHotelService hotelService;

    /**
     * @description: 监听酒店新增或修改的业务
     * @param id 酒店ID
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/18 21:36
     */
    @RabbitListener(queues = MqConstants.HOTEL_INSERT_QUEUE)
    public void listenerHotelInsertOrUpdate(Long id) {
        hotelService.insertById(id);
    }

    /**
     * @description: 监听酒店删除的业务
     * @param id 酒店ID
     * @return: void
     * @author: AmazeCode
     * @date: 2023/11/18 21:36
     */
    @RabbitListener(queues = MqConstants.HOTEL_DELETE_QUEUE)
    public void listenerHotelDelete(Long id) {
        hotelService.deleteById(id);
    }
}
