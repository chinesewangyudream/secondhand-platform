package com.secondhand.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.dto.*;
import com.secondhand.entity.User;
import com.secondhand.exception.BusinessException;
import com.secondhand.mapper.UserMapper;
import com.secondhand.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    public void register(RegisterDTO dto) {
        // 检查用户名是否存在
        if (lambdaQuery().eq(User::getUsername, dto.getUsername()).count() > 0) {
            throw new BusinessException("用户名「" + dto.getUsername() + "」已被使用，请更换其他用户名");
        }
        // 检查邮箱是否存在
        if (lambdaQuery().eq(User::getEmail, dto.getEmail()).count() > 0) {
            throw new BusinessException("邮箱「" + dto.getEmail() + "」已被注册，请直接登录或使用其他邮箱");
        }
        // 检查手机号是否存在（如果提供了）
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            if (lambdaQuery().eq(User::getPhone, dto.getPhone()).count() > 0) {
                throw new BusinessException("手机号「" + dto.getPhone() + "」已被注册");
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setCreditScore(100);
        user.setBalance(java.math.BigDecimal.ZERO);
        user.setStatus(1);
        user.setRole("USER");
        save(user);
    }

    /**
     * 用户登录（支持用户名或邮箱登录）
     */
    public Map<String, Object> login(LoginDTO dto) {
        String username = dto.getUsername();

        // 支持用户名或邮箱登录
        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .or()
                .eq(User::getEmail, username)
                .one();

        if (user == null) {
            throw new BusinessException("账号不存在，请检查用户名/邮箱是否输入正确，或点击「立即注册」创建新账号");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误，请重新输入。如忘记密码，请联系客服找回");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("您的账号已被禁用，如有疑问请联系客服处理");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole());
        return result;
    }

    /**
     * 获取用户信息（不含密码）
     */
    public Map<String, Object> getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在，请重新登录");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("phone", user.getPhone());
        info.put("avatar", user.getAvatar());
        info.put("nickname", user.getNickname());
        info.put("creditScore", user.getCreditScore());
        info.put("balance", user.getBalance());
        info.put("role", user.getRole());
        return info;
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在，请重新登录");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误，请重新输入");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }
}