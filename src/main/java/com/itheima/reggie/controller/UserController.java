package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName UserController
 * @Description
 * @Author xqr
 * @Date 2023/7/15 21:18
 * @Version 1.0
 */

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /*** 
     * @description: 发送手机验证码
     * @param: user
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/15 21:32
     */

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
            //调用阿里云提供的短信服务API完成发送短信
//            SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
            //需要将生成的验证码保存到Session
//            session.setAttribute(phone, code);
            //将生成的验证码存入Redis中，并且设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return R.success("手机验证码短信发送成功！");
        }

        return R.error("短信发送失败！");
    }

    /*** 
     * @description: 移动端用户登录验证
     * @param: user
    session
     * @return: com.itheima.reggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/7/16 9:45
     */

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {

        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从session中获取保存的验证码
//        Object codeInSession = session.getAttribute(phone);
        //从redis中获取缓存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);
        //进行验证码比对
        if (code != null && code.equals(codeInSession)) {
            //如果你能够比对成功，说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            //判断当前手机号是否为新用户，如果是新用户就自动完成注册
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());

            //用户登录成功，就删除Redis中的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> loginOut(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return R.success("退出成功！");
    }
}
