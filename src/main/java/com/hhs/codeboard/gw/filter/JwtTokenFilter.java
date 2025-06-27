package com.hhs.codeboard.gw.filter;


import com.hhs.codeboard.gw.dto.AuthDto;
import com.hhs.codeboard.gw.dto.CommonResponse;
import com.hhs.codeboard.gw.util.CookieUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

/**
 * JWT 인증용 필터
 * 일반 필터로 accessKey체크후 헤더에 유저정보를 심어보낸다.
 */
@Slf4j
@DependsOn({"commonConfig"})
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends AbstractGatewayFilterFactory<JwtTokenFilter.Config> {

    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory = new ModifyResponseBodyGatewayFilterFactory(new ArrayList<>(), new HashSet<>(), new HashSet<>());;
    private final WebClient authClient;

    // TODO :: 해당 내용도 application.yml 로 빼서 enum내부로 config 객체를 만들어 담아 넘겨줘야함.
    private static final String ACCESS_TOKEN_KEY = "CB_AT";
	private static final String REFRESH_TOKEN_KEY = "CB_RT";

    public record Config(FilterType filterType) {}

    // 요청이 들어오면 토큰이 있는지 확인.
    // 있을경우 에러 리턴.
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> config.filterType.process(exchange, chain, authClient);
    }

    private static void resetTokenExpired(ServerWebExchange exchange, AuthDto authDto) {
        exchange.getResponse().addCookie(CookieUtil.getCookie(ACCESS_TOKEN_KEY, authDto.getAccessToken(), Duration.ofHours(2)));
        exchange.getResponse().addCookie(CookieUtil.getCookie(REFRESH_TOKEN_KEY, authDto.getRefreshToken(), Duration.ofDays(7)));
    }

    interface TriFunction<T, R, U, Q> {
        Q apply(T t, R r, U u) throws RuntimeException;
    }

    /**
     * 토큰 유효성 결과는 email과 jwt타입이 있음.
     *
     * EMAIL은 헤더에 유저정보를 그대로 담아서 전달하는 방법으로 모든 요청이 GW를 통과하는 모듈에서만
     * 사용가능. 만약 gw외에서도 요청이오면 헤더로 손쉽게 요청 위조가 가능해진다.
     *
     * JWT는 헤더에 유저 정보를 jwt로 묶어서 전달해주는 방법.
     * 각 모듈에서는 auth모듈로 요청을 보내 유효성 검증을 진행 가능.
     * 외부 요청을 처리.
     */
    @RequiredArgsConstructor
    public enum FilterType {
        AUTH_EMAIL((exchange, chain, authClient)-> {
            AuthDto authDto = new AuthDto();
            authDto.setAccessToken(getToken(ACCESS_TOKEN_KEY, exchange));
            authDto.setRefreshToken(getToken(REFRESH_TOKEN_KEY, exchange));

            return Mono.defer(() -> authClient.post().uri("/gw/authorized/email")
                .bodyValue(authDto)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<CommonResponse<AuthDto>>() {})  //  이메일을 리턴값으로 준다. 없으면 실패.
            ).mapNotNull(afterAuthDto -> {
                AuthDto data = afterAuthDto.getData();
                // 헤더값 변환
                if (data != null) {
                    exchange.mutate().request(beforeRequest -> beforeRequest.header("X-USER-INFO", data.getEmail()).build());
                }
                // 쿠키 시간 업데이트
                resetTokenExpired(exchange, authDto);
                return afterAuthDto;
            }).then(chain.filter(exchange));
        })
        , AUTH_JWT((exchange, chain, authClient)->
            Mono.fromSupplier(()->{
                AuthDto authDto = new AuthDto();
                authDto.setAccessToken(getToken(ACCESS_TOKEN_KEY, exchange));
                authDto.setRefreshToken(getToken(REFRESH_TOKEN_KEY, exchange));
                return authDto;
            })
            .flatMap(authDto ->
                authClient.post().uri("/gw/authorized/jwt")
                    .body(authDto, AuthDto.class)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CommonResponse<AuthDto>>() {})  //  JWT를 리턴값으로 준다. 없으면 실패.
                    .onErrorReturn(null)
                    .mapNotNull(afterAuthDto->{
                        AuthDto data = afterAuthDto.getData();
                        // 헤더값 변환
                        if (data != null) {
                            exchange.mutate().request(beforeRequest->beforeRequest.header("X-USER-INFO", data.getJwt()).build());
                        }
                        // 쿠키 시간 업데이트
                        resetTokenExpired(exchange, authDto);
                        return afterAuthDto;
                    })
            ).then(chain.filter(exchange))
        )
        , AUTH_BEARER_ACCESS_TOKEN((exchange, chain, authClient)->
            // 다른 통신망과 연동이 필요한경우
            Mono.fromRunnable(()->
                exchange.mutate().request(beforeRequest->beforeRequest.header("Authorization", "Bearer " + getToken(ACCESS_TOKEN_KEY, exchange)).build())
            ).then(chain.filter(exchange))
        )
        , AUTH_BEARER_JWT((exchange, chain, authClient)->
            Mono.fromSupplier(()->{
                    AuthDto authDto = new AuthDto();
                    authDto.setAccessToken(getToken(ACCESS_TOKEN_KEY, exchange));
                    authDto.setRefreshToken(getToken(REFRESH_TOKEN_KEY, exchange));
                    return authDto;
                })
                .flatMap(authDto ->
                    authClient.post().uri("/gw/authorized/jwt")
                        .body(authDto, AuthDto.class)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<CommonResponse<AuthDto>>() {})    //  JWT를 리턴값으로 준다. 없으면 실패.
                        .onErrorReturn(new CommonResponse<>())
                        .mapNotNull(afterAuthDto->{
                            AuthDto data = afterAuthDto.getData();
                            // 헤더값 변환
                            if (data != null) {
                                exchange.mutate().request(beforeRequest->beforeRequest.header("Authorization", "Bearer " + data.getJwt()).build());
                            }
                            // 쿠기 시간 업데이트
                            resetTokenExpired(exchange, authDto);
                            return afterAuthDto;
                        })
                ).then(chain.filter(exchange))
        )
        ;

        private final TriFunction<ServerWebExchange, GatewayFilterChain, WebClient, Mono<Void>> filterProcess;

        public Mono<Void> process (ServerWebExchange ex, GatewayFilterChain ch, WebClient mc) {
            return this.filterProcess.apply(ex, ch, mc);
        }


        private static String getToken(String tokenKey, ServerWebExchange exchange) {
            return Optional.of(exchange.getRequest().getCookies())
                    .map(cookies -> cookies.getFirst(tokenKey))
                    .map(HttpCookie::getValue)
                    .orElse("");
        }

        private static void responseProcess() {

        }
    }
}