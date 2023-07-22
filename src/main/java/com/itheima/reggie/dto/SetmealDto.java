package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {
    //1.保存套餐中包含菜品的集合
    private List<SetmealDish> setmealDishes;
    //套餐类名称
    private String categoryName;
}
