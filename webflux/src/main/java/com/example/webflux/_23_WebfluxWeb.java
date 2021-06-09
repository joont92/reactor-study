package com.example.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootApplication
@RestController
public class _23_WebfluxWeb {
    static final String URL1 = "http://localhost:8081/service1?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";

    WebClient webClient = WebClient.create();

    @GetMapping("/rest")
    public Mono<String> rest(int idx) {
        return webClient.get().uri(URL1, idx).exchange()
                .flatMap(cr -> cr.bodyToMono(String.class));
    }

    public static void main(String[] args) {
        System.setProperty("reactor.ipc.netty.workerCount", "2");
        System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");

        SpringApplication.run(_23_WebfluxWeb.class, args);
    }
}
