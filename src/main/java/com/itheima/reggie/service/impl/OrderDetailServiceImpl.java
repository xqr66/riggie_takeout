package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.mapper.OrderDetailMapper;
import com.itheima.reggie.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @ClassName OrderDetailServiceImpl
 * @Description
 * @Author xqr
 * @Date 2023/7/18 16:49
 * @Version 1.0
 */

@Service
@Slf4j
public class OrderDetailServiceImpl extends ServiceImpl <OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
