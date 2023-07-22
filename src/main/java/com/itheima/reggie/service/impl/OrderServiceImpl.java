package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName OrderServiceImpl
 * @Description
 * @Author xqr
 * @Date 2023/7/18 16:48
 * @Version 1.0
 */

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;
    /*** 
     * @description: 用户下单
     * @param: orders
     * @return: void
     * @date: 2023/7/18 17:13
     */ 

    @Transactional
    @Override
    public void submit(Orders orders) {
        //获得用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        if(list == null || list.size() == 0) {
            throw new CustomException("购物车为空，无法下单");
        }
        //查询用户信息
        User user = userService.getById(userId);
        
        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null ) {
            throw new CustomException("用户地址信息有误，不能下单");
        }
        long orderId = IdWorker.getId();//订单号

        //使用原子类保证多线程的情况下不会出现计算错误
        AtomicInteger amount = new AtomicInteger(0);

        //遍历购物车数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for(ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setName(shoppingCart.getName());
            orderDetail.setImage(shoppingCart.getImage());
            orderDetail.setAmount(shoppingCart.getAmount());
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
            orderDetails.add(orderDetail);
        }


        //向订单表插入数据，一条数据
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);//2代表待派送
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);

        //向明细表插入数据，可能有多条
        orderDetailService.saveBatch(orderDetails);
        //清空购物车中数据
        shoppingCartService.remove(queryWrapper);
    }
    
    /*** 
     * @description: 根据订单id来获取详细的订单
     * @param: orderId
     * @return: java.util.List<com.itheima.reggie.entity.OrderDetail>
     * @date: 2023/7/19 10:41
     */ 
    
    @Override
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId) {
         LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
         queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailsList = orderDetailService.list(queryWrapper);
        return orderDetailsList;
    }
}
