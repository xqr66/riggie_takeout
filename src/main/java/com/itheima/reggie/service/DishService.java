package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品队友的口味数据，需要同时操作两张表，dish，dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品和口味信息
    public DishDto getByIdWithFlavor(Long id);
    public void updateByIdWithFlavor(DishDto dishDto);
}
