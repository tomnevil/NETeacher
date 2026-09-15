package com.neteacher.server.config;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器：校验 /api/**（登录与文档接口除外）。
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "缺少 Authorization 头");
        }
        String token = auth.substring(7);
        try {
            Claims claims = jwtUtil.parse(token);
            request.setAttribute("uid", claims.get("uid"));
            request.setAttribute("role", claims.get("role"));
        } catch (Exception e) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "登录已过期或无效");
        }
        return true;
    }
}
