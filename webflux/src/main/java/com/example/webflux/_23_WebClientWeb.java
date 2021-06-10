package com.example.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootApplication
@RestController
public class _23_WebClientWeb {
    public static void main(String[] args) {
        System.setProperty("reactor.ipc.netty.workerCount", "1"); // doesn't work
        System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");

        SpringApplication.run(_23_WebClientWeb.class, args);
    }

    static final String URL1 = "http://localhost:8081/service1?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";

    WebClient webClient = WebClient.create();

    @Autowired
    private MyService myService;

    @GetMapping("/rest")
    public Mono<String> rest(int idx) {
        return webClient.get().uri(URL1, idx).exchange()
                .flatMap(cr -> cr.bodyToMono(String.class))
                .doOnNext(log::info)
                .flatMap(res -> webClient.get().uri(URL2, res).exchange())
                .flatMap(cr -> cr.bodyToMono(String.class))
                .doOnNext(log::info)
                .flatMap(res -> Mono.fromCompletionStage(myService.work(res)))
                .doOnNext(log::info);
    }

    @Service
    public static class MyService {
        @Async
        public CompletableFuture<String> work(String req) {
            return CompletableFuture.completedFuture(req + " async worked");
        }
    }
}
