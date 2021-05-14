package toby;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Flow;

@SpringBootApplication
public class _07_PubSubControllerExample {
    public static void main(String[] args) {
        SpringApplication.run(_07_PubSubControllerExample.class, args);
    }

    @RestController
    static class Controller {
        @GetMapping("/hello")
        public Flow.Publisher<String> hello(String name) {
            return sub -> sub.onSubscribe(new Flow.Subscription() {
                // 1과 127 2번 찍히는데 왜지?
                @Override
                public void request(long n) {
                    sub.onNext("hello " + name);
                    sub.onComplete();
                }

                @Override
                public void cancel() {

                }
            });
        }
    }
}
