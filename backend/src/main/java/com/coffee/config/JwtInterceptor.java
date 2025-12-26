package com.coffee.config;

import com.coffee.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                // 简化：直接解析用户ID
                Long userId = userService.parseToken(token.substring(7));
                if (userId != null) {
                    request.setAttribute("userId", userId);
                    // 获取用户信息
                    var user = userService.getUserById(userId);
                    if (user != null) {
                        request.setAttribute("username", user.getUsername());
                        request.setAttribute("role", user.getRole());
                    }
                }
            } catch (Exception e) {
                // Token 无效，不设置用户信息
            }
        }
        return true;
    }
}
