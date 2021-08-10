package example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@SpringBootApplication
public class _17_DeferredResultWeb {
    public static void main(String[] args) {
        SpringApplication.run(_17_DeferredResultWeb.class, args);
    }

    @RestController
    public static class MyController {
        Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();

        @GetMapping("/dr")
        public DeferredResult<String> dr() {
            var dr = new DeferredResult<String>();
            this.results.add(dr);
            return dr;
        }

        @GetMapping("/dr/count")
        public String drCount() {
            return String.valueOf(results.size());
        }

        @GetMapping("/dr/event")
        public String drEvent(String msg) {
            for (DeferredResult<String> result : results) {
                result.setResult("HELLO " + msg);
                results.remove(result);
            }

            return "OK";
        }
    }
}
