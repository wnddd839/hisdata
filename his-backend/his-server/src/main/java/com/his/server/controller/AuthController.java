package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.*;
import com.his.server.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.his.server.utils.JwtUtils;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String SESSION_UID_KEY = "uid";
    public static final String SESSION_ROLE_KEY = "role";

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "注册 (患者)")
    @PostMapping("/register")
    public GlobalResult<AuthUserVO> register(@RequestBody AuthRegisterDTO dto) {
        AuthUserVO vo = authService.register(dto);
        String token = jwtUtils.generateToken(vo.getUserId(), vo.getPid(), vo.getPhone(), vo.getRole());
        vo.setToken(token);
        return GlobalResult.success(vo);
    }

    @Operation(summary = "注册 (患者) - 完整版")
    @PostMapping("/register/patient")
    public GlobalResult<AuthUserVO> registerPatient(@RequestBody AuthRegisterDTO dto) {
        AuthUserVO vo = authService.register(dto);
        String token = jwtUtils.generateToken(vo.getUserId(), vo.getPid(), vo.getPhone(), vo.getRole());
        vo.setToken(token);
        return GlobalResult.success(vo);
    }

    @Operation(summary = "注册 (患者) - 简化版，只需手机号和密码")
    @PostMapping("/register/patient/simple")
    public GlobalResult<AuthUserVO> registerPatientSimple(@RequestBody PatientRegisterDTO dto) {
        AuthUserVO vo = authService.registerPatientSimple(dto.getPhone(), dto.getPassword());
        String token = jwtUtils.generateToken(vo.getUserId(), vo.getPid(), vo.getPhone(), vo.getRole());
        vo.setToken(token);
        return GlobalResult.success(vo);
    }

    @Operation(summary = "注册 (医生)")
    @PostMapping("/register/doctor")
    public GlobalResult<AuthUserVO> registerDoctor(@RequestBody DoctorRegisterDTO dto) {
        AuthUserVO vo = authService.registerDoctor(dto);
        String token = jwtUtils.generateToken(vo.getUserId(), vo.getPid(), vo.getPhone(), vo.getRole());
        vo.setToken(token);
        return GlobalResult.success(vo);
    }

    @Operation(summary = "注册 (管理员)")
    @PostMapping("/register/admin")
    public GlobalResult<AuthUserVO> registerAdmin(@RequestBody AdminRegisterDTO dto) {
        AuthUserVO vo = authService.registerAdmin(dto.getPhone(), dto.getPassword());
        String token = jwtUtils.generateToken(vo.getUserId(), vo.getPid(), vo.getPhone(), vo.getRole());
        vo.setToken(token);
        return GlobalResult.success(vo);
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public GlobalResult<AuthUserVO> login(@RequestBody AuthLoginDTO dto) {
        AuthUserVO vo = authService.login(dto);
        // 生成JWT Token
        String token = jwtUtils.generateToken(vo.getUserId(), vo.getPid(), vo.getPhone(), vo.getRole());
        vo.setToken(token);
        return GlobalResult.success(vo);
    }

    @Operation(summary = "退出")
    @PostMapping("/logout")
    public GlobalResult<Void> logout() {
        // JWT是无状态的，客户端删除token即可
        return GlobalResult.success();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/userinfo")
    public GlobalResult<AuthUserVO> getUserInfo(HttpServletRequest request) {
        return me(request);
    }

    @Operation(summary = "获取登录日志")
    @GetMapping("/login-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<java.util.List<com.his.server.entity.LoginLog>> getLoginLogs() {
        return GlobalResult.success(authService.getLoginLogs());
    }

    @Operation(summary = "当前登录用户")
    @GetMapping("/me")
    public GlobalResult<AuthUserVO> me(HttpServletRequest request) {
        // 优先从JWT token获取用户信息
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Integer userId = jwtUtils.extractUserId(token);
                String role = jwtUtils.extractRole(token);
                String phone = jwtUtils.extractClaim(token, claims -> claims.get("phone", String.class));

                // 从数据库获取完整用户信息
                AuthUserVO vo = authService.getUserInfo(userId);
                if (vo != null) {
                    return GlobalResult.success(vo);
                }
            } catch (Exception e) {
                // JWT解析失败,尝试Session
            }
        }

        // Session fallback
        HttpSession session = request.getSession(false);
        if (session == null) {
            return GlobalResult.success(null);
        }
        Integer uid = (Integer) session.getAttribute(SESSION_UID_KEY);
        if (uid == null) {
            return GlobalResult.success(null);
        }

        // 从数据库获取完整用户信息
        AuthUserVO vo = authService.getUserInfo(uid);
        return GlobalResult.success(vo);
    }
}
