package com.itheima.reggie.common;

/**
 * @ClassName BaseContext
 * @Description 基于ThreadLocal封装工具类，用于保存和获取当前登录用户id
 * @Author xqr
 * @Date 2023/7/6 9:43
 * @Version 1.0
 */

public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
