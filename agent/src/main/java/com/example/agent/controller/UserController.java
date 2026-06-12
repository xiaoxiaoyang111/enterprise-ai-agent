package com.example.agent.controller;

import com.example.agent.common.Result;
import com.example.agent.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.agent.service.UserService;
// 接口暴露层
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO) {
        try {
            userService.register(userDTO);
            return Result.success("注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage() != null ? e.getMessage() : "系统内部异常(空指针)，请看IDEA控制台");
        }
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDTO userDTO) {
        try {
            String token = userService.login(userDTO);
            return Result.success(token);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage() != null ? e.getMessage() : "系统内部异常(空指针)，请看IDEA控制台");
        }
    }
}