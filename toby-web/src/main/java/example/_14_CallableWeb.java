package example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@Slf4j
@SpringBootApplication
@EnableAsync
public class _14_CallableWeb {
    public static void main(String[] args) {
        SpringApplication.run(_14_CallableWeb.class, args);
    }

    @RestController
    public static class MyController {
        @GetMapping("/basic")
        public String basic() throws InterruptedException {
            log.info("basic");
            Thread.sleep(2000);
            return "Hello";
        }

        @GetMapping("/callable")
        public Callable<String> callable() {
            log.info("callable");
            return () -> {
                log.info("async");
                Thread.sleep(2000);
                return "Hello";
            };
        }
    }
}
