package com.bfc.controller;

import com.bfc.entity.User;
import com.bfc.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        // 简单校验：用户名不能重复
        if (userService.findByUsername(user.getUsername()) != null) {
            return "用户名已存在";
        }
        userService.register(user);
        return "注册成功";
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        boolean success = userService.checkPassword(user.getUsername(), user.getPassword());
        return success ? "登录成功" : "用户名或密码错误";
    }

    /**
     * 按用户名查找用户
     */
    @GetMapping("/{username}")
    public User getUser(@PathVariable String username) {
        return userService.findByUsername(username);
    }
}
