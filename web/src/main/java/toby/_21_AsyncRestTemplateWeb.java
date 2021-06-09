package toby;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@SpringBootApplication
@EnableAsync
public class _21_AsyncRestTemplateWeb {
    public static void main(String[] args) {
        SpringApplication.run(_21_AsyncRestTemplateWeb.class, args);
    }

    @RestController
    public static class MyController {
        AsyncRestTemplate restTemplate =
                new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
        @Autowired MyService myService;

        @GetMapping("/rest")
        public ListenableFuture<ResponseEntity<String>> rest(int idx) {
            return restTemplate.getForEntity("http://localhost:8081/service1?req={req}", String.class, "hello " + idx);
        }

        // DeferredResult 를 사용하면 결과를 받아서 가공할 수 있다
        @GetMapping("/deferred-rest")
        public DeferredResult<String> deferredRest(int idx) {
            var result = new DeferredResult<String>();

            // 사용 쓰레드 총 3개 : netty thread 1개, bean으로 등록한 thread pool 1개, 서블릿 thread 1개
            // 단점: 비즈니스 로직과 멀티 쓰레드 관련 코드들이 같이 존재한다
            var f1 = restTemplate.getForEntity("http://localhost:8081/service1?req={req}",
                    String.class, "hello " + idx);
            f1.addCallback(s -> {
                var f2 = restTemplate.getForEntity("http://localhost:8081/service2?req={req}",
                        String.class, s.getBody());
                f2.addCallback(s2 ->
                    myService.work(s2.getBody()).addCallback(
                            result::setResult,
                            e3 -> result.setResult(e3.getMessage())),
                        e2 -> result.setResult(e2.getMessage()));
            }, e -> result.setResult(e.getMessage()));

            // 먼저 리턴되고 위의 AsyncRestTemplate의 워커 쓰레드에서 deferredResult.setResult 호출
            return result;
        }
    }

    @Service
    public static class MyService {
        @Async
        public ListenableFuture<String> work(String req) {
            return new AsyncResult<>(req + " async worked");
        }
    }

    @Bean
    public ThreadPoolTaskExecutor threadPool() {
        var tpe = new ThreadPoolTaskExecutor();
        tpe.setCorePoolSize(1);
        tpe.setMaxPoolSize(1);
        return tpe;
    }
}
