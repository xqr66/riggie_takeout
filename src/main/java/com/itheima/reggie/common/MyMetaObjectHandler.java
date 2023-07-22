package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @ClassName MyMetaObjectHandler
 * @Description 自定义元数据对象处理器
 * @Author xqr
 * @Date 2023/6/28 17:58
 * @Version 1.0
 */

@Slf4j
@Component//让spring框架来管理
public class MyMetaObjectHandler implements MetaObjectHandler {
    /** 
     * @description: 当插入/更新数据时的方法
     * @param: metaObject
     * @return: void
     * @author xqr
     * @date: 2023/6/28 18:00
     */
    @Override
    public void insertFill( MetaObject metaObject) {
        log.info("公共字段自动填充");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充");
        log.info(metaObject.toString());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());

    }
}
