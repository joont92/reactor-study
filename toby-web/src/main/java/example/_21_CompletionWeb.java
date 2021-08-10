package example;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
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

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
@EnableAsync
public class _21_CompletionWeb {
    public static void main(String[] args) {
        SpringApplication.run(_21_CompletionWeb.class, args);
    }

    @RestController
    public static class MyController {
        AsyncRestTemplate restTemplate =
                new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
        @Autowired MyService myService;

        @GetMapping("/completion-rest")
        public DeferredResult<String> completionRest(int idx) {
            var result = new DeferredResult<String>();

            Completion.from(restTemplate.getForEntity("http://localhost:8081/service1?req={req}", String.class, "hello " + idx))
                    .andApply(s -> restTemplate.getForEntity("http://localhost:8081/service2?req={req}", String.class, s.getBody()))
//                    .andApply(s -> myService.work(s))
                    .andError(e -> result.setErrorResult(e.getMessage()))
                    .andAccept(s -> result.setResult(s.getBody()));

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

    public static class ApplyCompletion extends Completion {
        private Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> function;

        public ApplyCompletion(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> function) {
            this.function = function;
        }

        @Override
        protected void run(ResponseEntity<String> s) {
            var lf = function.apply(s);
            lf.addCallback(super::complete, super::error);
        }
    }

    public static class ErrorCompletion extends Completion {

        private Consumer<Throwable> errorConsumer;

        public ErrorCompletion(Consumer<Throwable> consumer) {
            this.errorConsumer = consumer;
        }

        @Override
        protected void error(Throwable t) {
            errorConsumer.accept(t);
        }
    }

    public static class AcceptCompletion extends Completion {

        private Consumer<ResponseEntity<String>> consumer;

        public AcceptCompletion(Consumer<ResponseEntity<String>> consumer) {
            this.consumer = consumer;
        }

        @Override
        protected void run(ResponseEntity<String> s) {
            consumer.accept(s);
        }

    }

    public static class Completion {
        private Completion next;

        public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {
            var completion = new Completion();
            lf.addCallback(completion::complete, completion::error);

            return completion;
        }

        public Completion andApply(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> function) {
            var completion = new ApplyCompletion(function);
            this.next = completion;

            return completion;
        }

        public Completion andError(Consumer<Throwable> errorConsumer) {
            var completion = new ErrorCompletion(errorConsumer);
            this.next = completion;

            return completion;
        }

        public void andAccept(Consumer<ResponseEntity<String>> consumer) {
            this.next = new AcceptCompletion(consumer);
        }

        private void complete(ResponseEntity<String> s) {
            if (next != null) next.run(s);
        }

        protected void error(Throwable t) {
            if (next != null) next.error(t);
        }

        protected void run(ResponseEntity<String> s) {
            if (next != null) next.run(s);
        }
    }
}
