package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.bytecode.stackmap.TypeData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.LongFunction;

/**
 * @ClassName DishServiceImpl
 * @Description
 * @Author xqr
 * @Date 2023/7/6 14:16
 * @Version 1.0
 */

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /*** 
     * @description: 新增菜品同时保存口味数据
     * @param: dishDto
     * @return: void
     * @author xqr
     * @date: 2023/7/7 13:11
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        //获得当前菜品的ID
        Long dishId = dishDto.getId();
        //保存菜品的口味到菜品口味表dish_flavor
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor dishFlavor : flavors) {
            dishFlavor.setDishId(dishId);
        }
        //保存菜品口味数据到菜品口味表dish_flavor中
        dishFlavorService.saveBatch(flavors);
    }

    /*** 
     * @description: 根据id来查询菜品信息和口味
     * @param: id
     * @return: com.itheima.reggie.dto.DishDto
     * @author xqr
     * @date: 2023/7/7 16:41
     */

    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //根据id查询菜品口味信息
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /***
     * @description: 修改菜品，先删除数据再重新插入
     * @param: dishDto
     * @return: com.itheima.reggie.dto.DishDto
     * @author xqr
     * @date: 2023/7/7 17:34
     */

    @Override
    @Transactional
    public void updateByIdWithFlavor(DishDto dishDto) {
        //修改菜品的基本信息
        Long dishId = dishDto.getId();
        this.updateById(dishDto);
        //修改菜品的口味信息,先删除再添加
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(lambdaQueryWrapper);
        //添加发送过来的新口味信息
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        for (DishFlavor dishFlavor : dishFlavors) {
            dishFlavor.setDishId(dishId);
            dishFlavorService.save(dishFlavor);
        }
    }
}
