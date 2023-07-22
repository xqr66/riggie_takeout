package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName DishController
 * @Description
 * @Author xqr
 * @Date 2023/7/7 10:53
 * @Version 1.0
 */

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    /*** 
     * @description: 新增菜品
     * @param: dishDto
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/7 13:00
     */

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /***
     * @description: 菜品的分页展示
     * @param: page
    pageSize
    name
     * @return: com.itheima.reggie.common.R<com.baomidou.mybatisplus.extension.plugins.pagination.Page>
     * @author xqr
     * @date: 2023/7/7 16:27
     */
    @GetMapping("/page")
    public R<Page> pageR(int page, int pageSize, String name) {
        //接收来自前端的分页参数
        log.info("page = {}, pageSize = {},name = {}", page, pageSize, name);
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝,拷贝除了records之外的所有数据，即只拷贝Page的数据
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = new ArrayList<>();

        for (Dish record : records) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(record, dishDto);
            Long categoryId = record.getCategoryId();
            //根据id来查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //根据分类对象来获取分类名称
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
                list.add(dishDto);
            }
        }
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /*** 
     * @description: 根据菜品id查询菜品信息和口味信息
     * @param: id
     * @return: com.itheima.reggie.common.R<com.itheima.reggie.dto.DishDto>
     * @author xqr
     * @date: 2023/7/7 16:36
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /*** 
     * @description: 修改菜品
     * @param: dishDto
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/7 17:32
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateByIdWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /***
     * @description: 删除指定的菜品，可以一次删除多个
     * @param: ids 删除菜品的id
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/7 18:09
     */

    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        log.info("删除菜品，id为:{}", ids.toString());
        for (Long id : ids) {
            //删除菜品的基本信息
            dishService.removeById(id);
            //删除菜品的口味信息
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(lambdaQueryWrapper);
        }
        return R.success("删除菜品成功");
    }

    /***
     * @description: 根据id来批量停售和起售菜品, 如果停售菜品需要将与该菜品关联的套餐也停售
     * @param: status:要修改的目标状态 1：起售  0：停售
    ids: 要修改的菜品的id集合
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/15 10:03
     */
    @PostMapping("/status/{status}")
    public R<String> stauts(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(ids != null, Dish::getId, ids);
        //依据数据进行批量查询
        List<Dish> dishList = dishService.list(lambdaQueryWrapper);
        for (Dish dish : dishList) {
            if (status == 0) {//当菜品要停售时
                LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SetmealDish::getDishId, dish.getId());
                //获取该菜品关联套餐数据的数量
                int count = setmealDishService.count(queryWrapper);
                if (count > 0) {//有套餐与该菜品关联时
                    //获取该菜品与套餐数据关联的数据集合
                    List<SetmealDish> setmealDishesList = setmealDishService.list(queryWrapper);

                    //获取相关联的套餐的id集合
                    Set<Long> set = new HashSet<>();
                    for (SetmealDish setmealDish : setmealDishesList) {
                        set.add(setmealDish.getSetmealId());
                    }
                    setmealService.status(0, new ArrayList<>(set));
                }
            }
            if (dish != null) {
                dish.setStatus(status);
                dishService.updateById(dish);
            }
        }
        return R.success("状态修改成功！");
    }

    /*** 
     * @description: 根据条件查询对应的菜品数据
     * @param: dish
     * @return: com.itheima.reggie.common.R<java.util.List < com.itheima.reggie.entity.Dish>>
     * @author xqr
     * @date: 2023/7/15 10:58
     */
    @GetMapping("/list")
    public R<List<DishDto>> List(Dish dish) {
        //构造查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1（起售状态的菜品）
        lambdaQueryWrapper.eq(Dish::getStatus, 1);
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(lambdaQueryWrapper);
        List<DishDto> dishDtoList = new ArrayList<>();
        for (Dish dish1 : list) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish1, dishDto);
            Long categoryId = dish1.getCategoryId();
            //依据分类id来查询费雷对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //查询当前菜品的口味信息
            Long dishId = dish1.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, dishId);
            //SQL: select * from dish_flavor where dish_id = ?;
            List<DishFlavor> dishFlavorslist = dishFlavorService.list(queryWrapper);
            dishDto.setFlavors(dishFlavorslist);
            dishDtoList.add(dishDto);
        }
        return R.success(dishDtoList);
    }


}
