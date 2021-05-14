package toby;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@SpringBootApplication
@EnableAsync
public class _13_SpringAsync {
    public static void main(String[] args) {
        try (ConfigurableApplicationContext c = SpringApplication.run(_13_SpringAsync.class, args)) {
        }
    }

    @Component
    public static class MyService {
        @Async
//        @Async("tp")
        public ListenableFuture<String> hello() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(2000);
            return new AsyncResult<>( "Hello");
        }
    }

    @Bean
    // Executor, ExecutorService
    ThreadPoolTaskExecutor tp() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        /*
        - 첫번쨰 쓰레드 요청이 올 때 core pool size 만큼 쓰레드를 만들어놓는다
        - core pool size 가 꽉 찼을 경우 queue 를 채우게 되고, queue가 꽉 찼을 때 max pool size 만큼 쓰레드 개수를 늘린다
        - 큐 개수를 설정하지 않으면 큐 개수가 무한대로 max pool size가 의미가 없어지게 된다
         */
        te.setCorePoolSize(10);
        te.setMaxPoolSize(100);
        te.setQueueCapacity(200);

        // te.setKeepAliveSeconds(); : max pool size 에 의해 늘어난 쓰레드가 사용이 끝난 후 이 시간만큼 재할당이 안되면 삭제함
        // te.setTaskDecorator(); : 앞뒤로 로직을 붙고 싶을때
        te.setThreadNamePrefix("mythread");
        te.initialize();
        return te;
    }

    @Autowired
    MyService myService;

    @Bean
    ApplicationRunner run() { // 부트가 실행될 떄 실행됨(DI가 완료된 후)
        return arg -> {
            log.info("run()");
            var f = myService.hello();
            f.addCallback(System.out::println, e -> System.out.println(e.getMessage()));
            log.info("exit");
            Thread.sleep(2000); // JVM 종료를 막기 위함
        };
    }
}
