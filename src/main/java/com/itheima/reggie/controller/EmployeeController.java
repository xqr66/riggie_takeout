package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName EmployeeController
 * @Description
 * @Author xqr
 * @Date 2023/6/27 15:03
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * @description:员工登录
     * @param:
     * @return:
     * @author xqr
     * @date: 2023/6/27 15:50
     */

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1.将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据用户提交的username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3.判断有没有查询到，失败就返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }
        //4.密码比对，如果密码不一致就返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }
        //5.查看员工状态是否被禁用（0-->禁用，1-->启用）
        if (emp.getStatus() == 0) {
            return R.error("账号已被禁用");
        }
        //6.登录成功，将员工id存入Session并返回登录结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * @description:用户退出
     * @param: request
     * @return: com.itheima.riggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/6/27 17:38
     */

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }


    /**
     * @description: 新增用户
     * @param: request employee
     * @return: com.itheima.riggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/6/27 18:24
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        //设置初始密码123456，并且进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //获得当前登录用户的id
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("新增员工成功！");
    }

    /**
     * @description: 配置分页信息查询
     * @param: page pagesize
     * @return: com.itheima.riggie.common.R<com.baomidou.mybatisplus.extension.plugins.pagination.Page>
     * @author xqr
     * @date: 2023/6/28 10:21
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //接收来自前端的分页参数
        log.info("page = {}, pageSize = {},name = {}", page, pageSize, name);
        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * @description: 根据员工id来修改员工信息
     * @param: employee
     * @return: com.itheima.riggie.common.R<java.lang.String>
     * @author xqr
     * @date: 2023/6/28 11:44
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        //测试当前端发送请求能否接收
        log.info(employee.toString());
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * @description: 根据id来查询员工信息，为了满足添加和修改用户的需求
     * @param: id
     * @return: com.itheima.riggie.common.R<com.itheima.riggie.entity.Employee>
     * @author xqr
     * @date: 2023/6/28 17:43
     */

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工的信息");
    }
}
