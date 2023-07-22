package com.itheima.reggie.common;

/**
 * @ClassName CustomException
 * @Description 自定义异常类
 * @Author xqr
 * @Date 2023/7/6 14:44
 * @Version 1.0
 */

public class CustomException extends RuntimeException {
    //构造方法
    public CustomException(String Message) {
        super(Message);
    }
}
