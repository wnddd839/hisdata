package com.his.server.utils;

import com.his.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     */
    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Integer) {
            return (Integer) principal;
        }
        throw new BusinessException(401, "无法获取用户信息");
    }

    /**
     * 获取当前登录用户角色
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse(null);
    }

    /**
     * 判断是否是患者
     */
    public static boolean isPatient() {
        return "ROLE_PATIENT".equals(getCurrentUserRole());
    }

    /**
     * 判断是否是医生
     */
    public static boolean isDoctor() {
        return "ROLE_DOCTOR".equals(getCurrentUserRole());
    }

    /**
     * 判断是否是管理员
     */
    public static boolean isAdmin() {
        return "ROLE_ADMIN".equals(getCurrentUserRole());
    }
}
