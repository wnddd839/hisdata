package com.his.server.config;

import com.his.server.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        log.info("JWT Filter: uri={}, header={}", request.getRequestURI(), authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            log.info("JWT extracted: {}...", jwt.substring(0, Math.min(jwt.length(), 10)));
            try {
                if (jwtUtils.validateToken(jwt)) {
                    Integer userId = jwtUtils.extractUserId(jwt);
                    Integer pid = jwtUtils.extractPid(jwt);
                    log.info("JWT valid. uid={}, pid={}", userId, pid);

                    // Create Authentication
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Bridge to legacy Session-based controllers
                    // This ensures existing controllers that use request.getSession().getAttribute("uid") still work
                    HttpSession session = request.getSession(true);
                    log.info("JWT Filter: processing request {}, session={}", request.getRequestURI(), session.getId());
                    if (session.getAttribute("uid") == null) {
                         session.setAttribute("uid", userId);
                         session.setAttribute("pid", pid);
                         log.info("JWT validated. Populated HttpSession {} with uid: {}, pid: {}", session.getId(), userId, pid);
                    } else {
                         log.info("Session {} already has uid: {}", session.getId(), session.getAttribute("uid"));
                    }
                } else {
                    log.warn("JWT invalid");
                }
            } catch (Exception e) {
                log.warn("JWT processing failed: {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.info("No Bearer header found");
        }
        chain.doFilter(request, response);
    }
}
