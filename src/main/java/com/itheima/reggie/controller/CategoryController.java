package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName CategoryController
 * @Description 分类管理
 * @Author xqr
 * @Date 2023/7/6 10:24
 * @Version 1.0
 */

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /*** 
     * @description: 新增分类
     * @param: category
     * @return: com.itheima.riggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/6 10:33
     */ 
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category:{}", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }
    /*** 
     * @description: 分页查询
     * @param: page
     *         pageSize
     * @return: com.itheima.riggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/6 10:41
     */ 
    
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        //分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);
        //进行分页查询
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }
    
    /*** 
     * @description: 依据id删除分类
     * @param: id
     * @return: com.itheima.riggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/6 11:26
     */ 
    
    @DeleteMapping
    public R<String> delete(Long ids) {
        log.info("删除分类，id为：{}", ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    /***
     * @description: 修改分类信息
     * @param: category
     * @return: com.itheima.riggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/6 15:22
     */

    @PutMapping
    public R<String> put(@RequestBody Category category) {
        log.info("修改分类信息：{}",category);
        categoryService.updateById(category);
        return R.success("修改分类成功!");
    }


    /***
     * @description: 根据条件查询分类数据
     * @param: category 用于接收type，用于确定是菜品还是套餐
     * @return: com.itheima.riggie.common.R<java.util.List<com.itheima.riggie.entity.Category>>
     * @author xqr
     * @date: 2023/7/7 11:37
     */
    @GetMapping("/list")
    public R <List<Category>> listR(Category category) {
        //条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        lambdaQueryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(lambdaQueryWrapper);
        return R.success(list);
    }
}
