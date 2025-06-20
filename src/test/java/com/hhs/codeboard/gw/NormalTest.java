package com.hhs.codeboard.blog.gw;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NormalTest {

    @Test
    public void TimeTest() {

    }

    @Test
    public void OptionalTest() {

//    Mono.fromSupplier(TestVO::new)
//            .map(vo->vo.setName(vo.getName()))
//        Flux<String> testFlux = Flux.concat(getName(), getNum())
//                .flatMap;

        TestVO test = new TestVO();
        test.setName("1");
        test.setNum("2");

        Mono.just(test)
                .flatMap(data->{
                    Mono<TestVO> test1 = getZipTest(data);
                    System.out.println("checker1");
                    return test1;
                })
                .map(data->{
                    System.out.println("checker2");
                    return data;
                })
                .subscribe();


    }

    @Getter
    @Setter
    static class TestVO {
        private String name;
        private String num;
    }

    private Mono<String> getName() {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            //
        }
        return Mono.just("name");
    }

    private Mono<String> getNum() {
        return Mono.just("num");
    }

    private Mono<TestVO> getTest() {
        return Mono.fromSupplier(()->{
           TestVO vo = new TestVO();
           vo.setNum("1");
           vo.setName("1");
           return vo;
        });
    }

    private Mono<TestVO> getZipTest(TestVO test) {
        return Mono.zip(getTest(), getTest())
                .map(tup->{
                    System.out.println("wait!");
                    return new TestVO();
                });
    }

}
