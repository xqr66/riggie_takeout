package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName SetmealServiceImpl
 * @Description
 * @Author xqr
 * @Date 2023/7/6 14:18
 * @Version 1.0
 */

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /*** 
     * @description: 新增套餐，需要同时保存套餐和菜品的关联关系
     * @param: setmealDto
     * @return: void
     * @author xqr
     * @date: 2023/7/15 15:24
     */
    @Transactional//开启事务
    public void saveWithDish(SetmealDto setmealDto) {
        //1. 保存套餐的基本信息（名称，价格等）,操作setmeal，执行insert操作
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        //2. 保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }
    /*** 
     * @description: 删除套餐同时删除套餐二号菜品的关联数据
     * @param: ids
     * @return: void
     * @author xqr
     * @date: 2023/7/15 17:08
     */ 
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐的状态，确定是否可以删除（必须停售状态才能删除）
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        //如果不能删除，抛出一个业务异常
        int count = this.count(queryWrapper);
        if(count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据--setmeal
        this.removeByIds(ids);

        //删除关系表中的数据
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper1);
    }

    @Override
    public void status(Integer status, List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        List<Setmeal> setmealList = this.list(queryWrapper);
        for(Setmeal setmeal : setmealList) {
            if(setmeal != null) {
                setmeal.setStatus(status);
                this.updateById(setmeal);
            }
        }
    }
    /***
     * @description: 回显套餐数据，根据套餐id查询套餐
     * @param: id
     * @return: com.itheima.reggie.dto.SetmealDto
     * @author xqr
     * @date: 2023/7/15 22:23
     */

    @Override
    public SetmealDto getData(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //在表中查询该套餐包含的菜品数据
        queryWrapper.eq(id != null, SetmealDish::getSetmealId, id);

        if(setmeal != null) {
            BeanUtils.copyProperties(setmeal, setmealDto);
            List<SetmealDish> list = setmealDishService.list(queryWrapper);
            setmealDto.setSetmealDishes(list);
            return setmealDto;
        }

        return null;
    }


}
