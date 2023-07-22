package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.handler.LogicalHandler;
import java.util.List;


/**
 * @ClassName ShoppingCartController
 * @Description
 * @Author xqr
 * @Date 2023/7/18 11:38
 * @Version 1.0
 */

@RestController
@Slf4j
@RequestMapping("shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /*** 
     * @description: 用于处理往购物车中添加菜品或者套餐
     * @param: shoppingCart
     * @return: com.itheima.reggie.common.R<com.itheima.reggie.entity.ShoppingCart>
     * @date: 2023/7/18 16:15
     */ 
    
    @PostMapping("add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}", shoppingCart);
        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (dishId != null) {
            //添加的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
            queryWrapper.eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor()) ;
        } else {
            //添加的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //SQl:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?;
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);

        if (shoppingCartOne != null) {
            //如果已经存在，就在原来的基础上加一
            Integer number = shoppingCartOne.getNumber();
            shoppingCartOne.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCartOne);
        } else {
            //如果不存在，就添加到购物车，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            shoppingCartOne = shoppingCart;
        }
        return R.success(shoppingCartOne);
    }
    
    /*** 
     * @description: 处理减少购物车中菜品或套餐的数量
     * @param: shoppingCart
     * @return: com.itheima.reggie.common.R<com.itheima.reggie.entity.ShoppingCart>
     * @date: 2023/7/18 16:15
     */ 
    
    @PostMapping("sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据：{}", shoppingCart);
        //判断减少的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (dishId != null) {
            //减少的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //减少的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);
        Integer number = shoppingCartOne.getNumber();
        if(number.equals(1)) {
            //如果购物车中只有一个当前减少的菜品或者套餐
            shoppingCartService.remove(queryWrapper);
        }else{
            shoppingCartOne.setNumber(number - 1);
            shoppingCartService.updateById(shoppingCartOne);
        }

        return R.success(shoppingCartOne);
    }
    
    /*** 
     * @description: 显示购物车数据
     * @param: 
     * @return: com.itheima.reggie.common.R<java.util.List<com.itheima.reggie.entity.ShoppingCart>>
     * @date: 2023/7/18 16:17
     */ 
    
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /*** 
     * @description: 清空购物车
     * @param: 
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @date: 2023/7/18 16:29
     */ 
    
    @DeleteMapping("/clean")
    public R<String> clean() {
        //SQL: delete from shopping_cart where user_id = ?;
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
