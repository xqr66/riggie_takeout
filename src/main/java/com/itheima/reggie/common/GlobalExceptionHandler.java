package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @ClassName GlobalExceptionHandler
 * @Description 全局异常代理,选择需要处理的Controller的范围
 * @Author xqr
 * @Date 2023/6/28 9:38
 * @Version 1.0
 */

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    
    /** 
     * @description: 处理sql重复主键异常
     * @param: ex
     * @return: com.itheima.riggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/6/28 9:46
     */ 
    
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已经存在";
            return R.error(msg);
        }
        return R.error("失败了");

    }
    /*** 
     * @description: 处理删除分类时，分类中关联菜品和套餐的异常
     * @param: ex
     * @return: com.itheima.riggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/6 14:50
     */ 
    
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        log.error(ex.getMessage());
        return R.error(ex.getMessage());

    }
}
