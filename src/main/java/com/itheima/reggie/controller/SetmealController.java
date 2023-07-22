package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName SetmealCOntroller
 * @Description 套餐管理的控制层
 * @Author xqr
 * @Date 2023/7/15 10:13
 * @Version 1.0
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /***
     * @description: 添加套餐，需要使用SetmealDto来接收参数，因为Dto不仅有套餐的基本参数，还有套餐中SetMealDish集合
     * @param: setmealDto
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/15 11:17
     */
    @PostMapping
    public R<String> addSetmeal(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("添加菜品成功！");
    }
    
    /*** 
     * @description: 套餐分页查询
     * @param:  page  当前页号
                pageSize 每一页的大小
                name 模糊查询关键词
     * @return: com.itheima.reggie.common.R<com.baomidou.mybatisplus.extension.plugins.pagination.Page>
     * @author xqr
     * @date: 2023/7/15 15:51
     */ 
    
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        //只使用Setmeal只包含分类id，不能显示分类名称，所以需要用Dto类
        Page<SetmealDto> dtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);

        //添加排序条件，根据更新时间来降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝,先拷贝除了记录数据之外的所有信息（页号，页面大小等）
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> dtoList = new ArrayList<>();
        for(Setmeal setmeal : records) {
            SetmealDto setmealDto = new SetmealDto();
            //先将setmeal中基本信息拷贝
            BeanUtils.copyProperties(setmeal, setmealDto);

            //获取分类的id
            Long categoryId = setmeal.getCategoryId();
            //获取分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null) {
                //获取菜品对应的分类名称
                String categoryName = category.getName();
                //设置 dto对象的分类名称
                setmealDto.setCategoryName(categoryName);
            }
            dtoList.add(setmealDto);
        }//end for
        dtoPage.setRecords(dtoList);
        return R.success(dtoPage);
    }

    /***
     * @description: 批量删除套餐
     * @param: ids 删除菜品的id
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/7 18:09
     */

    @DeleteMapping
    //用集合来接收需要添加@RequetParam注解，或者用Long[]数组来添加
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
      setmealService.removeWithDish(ids);

      return R.success("套餐数据删除成功");
    }

    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {
        setmealService.status(status, ids);
        return R.success("套餐状态修改成功！");
    }
    
    /*** 
     * @description: 回显套餐数据，根据套餐id查询套餐
     * @param: id
     * @return: com.itheima.reggie.common.R<com.itheima.reggie.dto.SetmealDto>
     * @author xqr
     * @date: 2023/7/15 22:20
     */ 
    
   @GetMapping("/{id}")
    public R<SetmealDto> getData(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getData(id);
        return R.success(setmealDto);
   }
    /***
     * @description: 列出该分类中所有的套餐
     * @param: setmeal
     * @return: com.itheima.reggie.common.R<java.util.List<com.itheima.reggie.entity.Setmeal>>
     * @date: 2023/7/19 13:38
     */

   @GetMapping("/list")
   public R<List<Setmeal>> listR(Setmeal setmeal) {
       //构造查询条件,返回分类信息相同的套餐
       LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
       queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
       //添加条件，查询状态为1（起售状态）的菜品
       queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, 1);

       //添加排序条件
       queryWrapper.orderByDesc(Setmeal::getUpdateTime);

       List<Setmeal> list = setmealService.list(queryWrapper);
       return R.success(list);
   }
    /***
     * @description: 修改套餐的菜品，需要同时修改套餐信息和套餐与菜品的关联信息
     * @param: setmealDto
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @date: 2023/7/19 13:40
     */

   @PutMapping
    public R<String> edit(@RequestBody SetmealDto setmealDto) {
       if(setmealDto == null) {
           return R.error("请求异常");
       }
       if(setmealDto.getSetmealDishes() == null) {
           return R.error("套餐中没有菜品，请重新添加");
       }

       //查询并删除数据库中原有的套餐与菜品关联信息
       List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
       Long setmealId = setmealDto.getId();
       LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
       queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
       setmealDishService.remove(queryWrapper);
       //为所有新的菜品进行的套餐id赋值
       for(SetmealDish setmealDish : setmealDishes) {
           setmealDish.setSetmealId(setmealDto.getId());
       }
       setmealDishService.saveBatch(setmealDishes);
       setmealService.updateById(setmealDto);

       return R.success("修改套餐成功！");
   }


    /**
     * 移动端点击套餐图片查看套餐具体内容
     * 这里返回的是dto 对象，因为前端需要copies这个属性
     * 前端主要要展示的信息是:套餐中菜品的基本信息，图片，菜品描述，以及菜品的份数
     * @param SetmealId 套餐名称
     * @return
     */
    //这里前端是使用路径来传值的，要注意，不然你前端的请求都接收不到，就有点尴尬哈
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable("id") Long SetmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,SetmealId);
        //获取与该套餐关联的所有菜品的集合
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtos = list.stream().map((setmealDish) -> {
            DishDto dishDto = new DishDto();
            //其实这个BeanUtils的拷贝是浅拷贝，这里要注意一下
            BeanUtils.copyProperties(setmealDish, dishDto);
            //这里是为了把套餐中的菜品的基本信息填充到dto中，比如菜品描述，菜品图片等菜品的基本信息
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);
    }
}
