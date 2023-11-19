package com.amazecode.hotel.service.impl;


import com.amazecode.hotel.mapper.HotelMapper;
import com.amazecode.hotel.pojo.Hotel;
import com.amazecode.hotel.service.IHotelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
}
