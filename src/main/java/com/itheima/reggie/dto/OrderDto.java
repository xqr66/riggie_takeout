package com.itheima.reggie.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import lombok.Data;
import java.util.List;

/**
 * @ClassName OrderDto
 * @Description
 * @Author xqr
 * @Date 2023/7/19 10:34
 * @Version 1.0
 */
@Data
public class OrderDto extends Orders  {

    private List<OrderDetail> orderDetails;
}