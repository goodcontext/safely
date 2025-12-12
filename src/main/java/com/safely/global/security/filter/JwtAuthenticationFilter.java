package com.safely.global.security.filter;

import com.safely.global.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // whiteList URL 들이 나오면 다음 필터로 바로 넘어감. 아래 설명 참고.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        String[] whiteList = {
                "/",
                "/health",
                "/api/auth/**",
                "/h2-console/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**"
        };

        for (String pattern : whiteList) {
            if (pathMatcher.match(pattern, path)) {
                return true;    // 여기서 doFilter() 안 했는데, 어떻게 다음 필터로 넘어가는지 아래 주석으로 설명함.
            }
        }
        return false;
    }

//    // OncePerRequestFilter의 내부를 보면 doFilter() 메소드에 아래의 코드가 포함되어 있음.
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//
//        // 이 코드를 보면 ShouldNotFilter 값이 TRUE이면 doFilter() 명령을 실행하도록 OncePerRequestFilter에 포함되어 있음.
//        if (shouldNotFilter(httpRequest)) {
//            chain.doFilter(request, response);
//            return;
//        }
//
//        doFilterInternal(httpRequest, (HttpServletResponse) response, chain);
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        // 토큰 없는 요청일 때 if문 실행이 안 되므로 doFilter() 메소드 실행되어 다음 필터로 넘어감.
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {

            // 로그아웃된 토큰인지 Redis 블랙리스트 확인
            String isLogout = redisTemplate.opsForValue()
                    .get("blacklist:access:" + token);
            if (isLogout == null) {
                Authentication authentication =
                        jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
