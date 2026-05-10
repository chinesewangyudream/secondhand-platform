package com.secondhand.controller;

import com.secondhand.dto.*;
import com.secondhand.entity.User;
import com.secondhand.service.UserService;
import com.secondhand.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String uploadUrlPrefix;

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
    public Result<?> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
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

    @PutMapping("/phone")
    public Result<?> changePhone(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        String newPhone = body.get("newPhone");
        userService.changePhone(SecurityUtils.getCurrentUserId(), password, newPhone);
        return Result.success("手机号换绑成功", null);
    }

    @PutMapping("/email")
    public Result<?> changeEmail(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        String newEmail = body.get("newEmail");
        userService.changeEmail(SecurityUtils.getCurrentUserId(), password, newEmail);
        return Result.success("邮箱换绑成功", null);
    }

    @PostMapping("/avatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error(400, "请选择要上传的图片");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error(400, "只能上传图片文件");
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 解析上传目录的绝对路径（与 WebMvcConfig 保持一致）
            File baseDir = new File(uploadPath);
            if (!baseDir.isAbsolute()) {
                baseDir = new File(System.getProperty("user.dir"), uploadPath);
            }
            File avatarDir = new File(baseDir, "avatars");
            if (!avatarDir.exists()) {
                avatarDir.mkdirs();
            }
            File dest = new File(avatarDir, filename);
            file.transferTo(dest.getAbsoluteFile());

            // 更新用户头像URL
            String avatarUrl = uploadUrlPrefix + "avatars/" + filename;
            Long userId = SecurityUtils.getCurrentUserId();
            User user = new User();
            user.setId(userId);
            user.setAvatar(avatarUrl);
            userService.updateById(user);

            return Result.success("头像上传成功", avatarUrl);
        } catch (IOException e) {
            return Result.error(500, "头像上传失败: " + e.getMessage());
        }
    }
}