package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /*** 
     * @description: 新增套餐同时需要保存套餐和菜品的关联关系
     * @param: setmealDto
     * @return: void
     * @author xqr
     * @date: 2023/7/15 15:23
     */ 
    
    public void saveWithDish(SetmealDto setmealDto);
    
    /*** 
     * @description: 删除套餐并且 删除套餐和菜品的关联数据
     * @param: ids
     * @return: void
     * @author xqr
     * @date: 2023/7/15 17:07
     */
    public void removeWithDish(List<Long> ids);

    public void status(Integer status,  List<Long> ids);

    public SetmealDto getData(Long id);


}
