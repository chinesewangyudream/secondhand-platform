package com.secondhand.controller;

import com.secondhand.dto.*;
import com.secondhand.service.UserService;
import com.secondhand.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/profile")
    public Result<?> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody com.secondhand.entity.User user) {
        user.setId(SecurityUtils.getCurrentUserId());
        user.setPassword(null); // 不允许在此接口改密码
        userService.updateById(user);
        return Result.success("更新成功", null);
    }

    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.changePassword(SecurityUtils.getCurrentUserId(), oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }
}