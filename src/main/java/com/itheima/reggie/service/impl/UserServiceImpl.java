package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.UserMapper;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @ClassName UserServiceImpl
 * @Description
 * @Author xqr
 * @Date 2023/7/15 21:17
 * @Version 1.0
 */

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl <UserMapper, User> implements UserService {
}
