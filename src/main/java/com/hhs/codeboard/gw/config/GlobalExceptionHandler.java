//package com.hhs.codeboard.gw.config;
//
//import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
///**
// * 글로벌 exception 처리
// */
//
//@Component
//public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
//    @Override
//    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
//        // 여기서 ex의 클래스 체크를해서 분기해도 되나... 원하는거랑은 좀 다른듯
//        return null;
//    }
//}
