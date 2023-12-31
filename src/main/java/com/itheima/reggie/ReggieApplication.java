package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @ClassName ReggieApplication
 * @Description
 * @Author xqr
 * @Date 2023/6/27 14:26
 * @Version 1.0
 */
@Slf4j
@ServletComponentScan
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching //开启Spring Cache注解方式的缓存功能
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功！");
    }
}
