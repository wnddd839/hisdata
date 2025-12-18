package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.AuthLoginDTO;
import com.his.server.dto.AuthRegisterDTO;
import com.his.server.dto.AuthUserVO;
import com.his.server.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.his.server.utils.JwtUtils;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String SESSION_UID_KEY = "uid";
    public static final String SESSION_PID_KEY = "pid";

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public GlobalResult<AuthUserVO> register(@RequestBody AuthRegisterDTO dto, HttpServletRequest request) {
        AuthUserVO vo = authService.register(dto);
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_UID_KEY, vo.getUserId());
        session.setAttribute(SESSION_PID_KEY, vo.getPid());
        
        // Generate Token
        String token = jwtUtils.generateToken(vo.getUserId(), vo.getPid(), vo.getPhone());
        vo.setToken(token);
        
        return GlobalResult.success(vo);
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public GlobalResult<AuthUserVO> login(@RequestBody AuthLoginDTO dto, HttpServletRequest request) {
        AuthUserVO vo = authService.login(dto);
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_UID_KEY, vo.getUserId());
        session.setAttribute(SESSION_PID_KEY, vo.getPid());
        
        // Generate Token
        String token = jwtUtils.generateToken(vo.getUserId(), vo.getPid(), vo.getPhone());
        vo.setToken(token);
        
        return GlobalResult.success(vo);
    }

    @Operation(summary = "退出")
    @PostMapping("/logout")
    public GlobalResult<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return GlobalResult.success();
    }

    @Operation(summary = "当前登录用户")
    @GetMapping("/me")
    public GlobalResult<java.util.Map<String, Object>> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return GlobalResult.success(null);
        }
        Integer uid = (Integer) session.getAttribute(SESSION_UID_KEY);
        Integer pid = (Integer) session.getAttribute(SESSION_PID_KEY);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("userId", uid);
        data.put("pid", pid);
        return GlobalResult.success(data);
    }
}
