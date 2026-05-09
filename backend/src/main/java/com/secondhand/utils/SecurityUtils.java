package com.secondhand.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import io.jsonwebtoken.Claims;

public class SecurityUtils {

    private static final ThreadLocal<Claims> claimsThreadLocal = new ThreadLocal<>();

    public static void setClaims(Claims claims) {
        claimsThreadLocal.set(claims);
    }

    public static void clearClaims() {
        claimsThreadLocal.remove();
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("未登录: 认证信息为空");
        }
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object details = token.getDetails();
            if (details instanceof Long) {
                return (Long) details;
            } else if (details instanceof Integer) {
                return ((Integer) details).longValue();
            } else if (details instanceof Number) {
                return ((Number) details).longValue();
            } else if (details != null) {
                throw new RuntimeException("用户ID类型错误: " + details.getClass().getName());
            } else {
                throw new RuntimeException("认证信息中未找到用户ID");
            }
        }
        throw new RuntimeException("未登录: 认证类型不符: " + auth.getClass().getName());
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) return auth.getName();
        throw new RuntimeException("未登录");
    }

    public static String getCurrentUserRole() {
        Claims claims = claimsThreadLocal.get();
        if (claims != null) {
            return claims.get("role", String.class);
        }
        throw new RuntimeException("未登录: 无法获取用户角色");
    }

    public static boolean isAuditor() {
        try {
            String role = getCurrentUserRole();
            return "ADMIN".equals(role) || "AUDITOR".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    public static void requireAuditor() {
        if (!isAuditor()) {
            throw new RuntimeException("无权限：需要审核员或管理员角色");
        }
    }
}