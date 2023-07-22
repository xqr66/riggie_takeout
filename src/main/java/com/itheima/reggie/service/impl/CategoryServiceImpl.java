package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName CateGoryServiceImpl
 * @Description
 * @Author xqr
 * @Date 2023/7/6 10:20
 * @Version 1.0
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /***
     * @description: 根据id删除分类，删除之前需要进行判断
     * @param: id
     * @return: void
     * @author xqr
     * @date: 2023/7/6 14:24
     */
    public void remove(Long id) {
        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishQueryWrapper);
        if (count1 > 0) {
            //已经关联菜品，抛出一个业务异常
            throw new CustomException("当前分类中关联了菜品，无法删除");
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常

        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealQueryWrapper);
        if(count2 > 0) {
            //已经关联套餐，抛出一个业务异常
            throw new CustomException("当前分类中关联了套餐，无法删除");
        }
        //正常删除分类
        removeById(id);
    }
}
