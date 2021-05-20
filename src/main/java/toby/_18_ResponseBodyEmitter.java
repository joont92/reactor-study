package toby;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
public class _18_ResponseBodyEmitter {
    public static void main(String[] args) {
        SpringApplication.run(_18_ResponseBodyEmitter.class, args);
    }

    @RestController
    public static class MyController {
        @GetMapping("/emitter")
        // http를 여러번에 나눠서 보내주는 sse 기술을 사용할 수 있다
        public ResponseBodyEmitter emitter() {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();

            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        emitter.send("<p>emitted response " + i + "<p/>");
                        Thread.sleep(1000);
                    }
                } catch (Exception e) { }
            });

            return emitter;
        }
    }
}
