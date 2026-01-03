package com.his.server.config;

import com.his.server.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 只处理JWT token
        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                if (jwtUtils.validateToken(jwt)) {
                    Integer userId = jwtUtils.extractUserId(jwt);
                    String role = jwtUtils.extractRole(jwt);

                    List<SimpleGrantedAuthority> authorities = role != null
                            ? Collections.singletonList(new SimpleGrantedAuthority(role))
                            : Collections.emptyList();

                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("JWT认证成功: userId={}, role={}", userId, role);
                } else {
                    log.warn("JWT token无效");
                }
            } catch (Exception e) {
                log.warn("JWT处理失败: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
