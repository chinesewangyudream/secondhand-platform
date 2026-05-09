package com.secondhand.controller;

import com.secondhand.dto.Result;
import com.secondhand.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}/info")
    public Result<?> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }
}