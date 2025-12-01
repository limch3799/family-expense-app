package com.example.d105.security.filter;

import com.example.d105.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JWTUtil jwtUtil;

    // 인증을 건너뛸 URL 패턴들
    private static final List<String> EXCLUDE_URL_PATTERNS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/verify/",  // verify로 시작하는 모든 경로
            "/swagger-ui/",
            "/v3/api-docs/",
            "/swagger-ui.html",
            "/actuator/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 제외할 URL 패턴에 해당하는지 확인
        boolean shouldSkip = EXCLUDE_URL_PATTERNS.stream()
                .anyMatch(pattern -> path.startsWith(pattern));

        if (shouldSkip) {
            log.debug("JWT 필터 건너뛰기: {}", path);
        }

        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("JWT 필터 실행: {}", request.getRequestURI());

        String jwt = resolveToken(request);

        if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
            Authentication authentication = jwtUtil.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT 인증 성공: {}", authentication.getName());
        } else if (StringUtils.hasText(jwt)) {
            log.debug("유효하지 않은 JWT 토큰");
        } else {
            log.debug("JWT 토큰이 없음");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}