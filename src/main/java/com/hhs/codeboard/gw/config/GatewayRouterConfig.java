package com.hhs.codeboard.gw.config;

import com.hhs.codeboard.gw.filter.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayRouterConfig {

    private final JwtTokenFilter jwtLoginFilter;

    @Value("${codeboard.member}")
    private String memberUrl;

    @Value("${codeboard.auth}")
    private String authUrl;

    @Value("${codeboard.blog}")
    private String blogUrl;

    /**
     * 라우트 빈 생성
     *
     * /{module}/public/ 일시에는 필터를 태우지 않는다.
     * /{module}/private/ 일시에는 필터를 태운다.
     *
     * TODO :: HHS
     * 어짜피 각 모듈에서 한번더 체크할테지만,
     * gateway의 부담을 줄일수 있다.
     *
     * 일단은 따로 처리 안함.
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator defaultRouter(RouteLocatorBuilder builder) {
        return builder
            .routes()
            .route("login-service",
                r->r.path("/auth/**")
//                    .filters(f1->f1.stripPrefix(3))
                    .uri(authUrl)
            )
            .route("user-service",
                r->r.path("/api/member/**")
                    .and()
                    .not(inR->inR.path("/api/member/gw/**"))
                    .filters(
                        f1->f1.stripPrefix(2)
                            .filter(jwtLoginFilter.apply(new JwtTokenFilter.Config(JwtTokenFilter.FilterType.AUTH_EMAIL)))
                    )
                    .uri(memberUrl)
            )
            .route("blog-service",
                r->r.path("/api/blog/**")
                    .filters(
                        f1->f1.stripPrefix(2)
                            .filter(jwtLoginFilter.apply(new JwtTokenFilter.Config(JwtTokenFilter.FilterType.AUTH_EMAIL)))
                    )
                    .uri(this.blogUrl)
            )
            .build();
    }
}
