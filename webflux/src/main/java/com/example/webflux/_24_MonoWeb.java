package com.example.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootApplication
@RestController
public class _24_MonoWeb {
    public static void main(String[] args) {
        SpringApplication.run(_24_MonoWeb.class, args);
    }

    @GetMapping("/hello1")
    public Mono<String> hello1() {
        log.info("pos1");
        var mono = Mono.just("hello1").log(); // publisher
        log.info("pos2");
        return mono; // return 후 spring에서 subscribe
    }

    @GetMapping("/hello2")
    public Mono<String> hello2() {
        log.info("pos1");
        var mono = Mono.just(generateHello()).log(); // 1
        log.info("pos2");
        return mono; // 2
    }

    @GetMapping("/hello3")
    public Mono<String> hello3() {
        log.info("pos1");
        var mono = Mono.fromSupplier(this::generateHello).log(); // 2
        log.info("pos2");
        return mono; // 1
    }

    @GetMapping("/hello4")
    public Mono<String> hello4() {
        log.info("pos1");
        var mono = Mono.fromSupplier(this::generateHello).log();
        mono.subscribe();
        log.info("pos2");
        return mono;
    }

    @GetMapping("/hello5")
    public Mono<String> hello5() {
        log.info("pos1");
        var mono = Mono.fromSupplier(this::generateHello).log();
        var s = mono.block();
        log.info("pos2 : " + s);
        return Mono.just(s); // mono를 그대로 리턴하면 다시 supplier 코드를 실행하게 되므로, 결과값을 그대로 just에 넣어서 사용
    }

    private String generateHello() {
        log.info("generateHello called"); // 2
        return "hello2"; // 4
    }
}
