package example;

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
        // 반환되는 Mono(publisher)에 map으로 계속 기능을 덧붙인 뒤, 스프링이 이를 subscribe하면서 수행
        // (MapOperator 처럼 onXXX 호출할 때 반환되는 값에 기능을 추가한 것이라고 보면 된다)
        // exchange 할 때 netty의 워커 쓰레드를 사용하고, myService 호출할 때 서비스에 선언해 둔 쓰레드 풀에서 쓰레드를 가져와 사용
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
