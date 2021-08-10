package example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class _15_LoadTest {
    public static void main(String[] args) throws InterruptedException {
//        loadTest("http://localhost:8080/basic");
        loadTest("http://localhost:8080/callable");
    }

    private static void loadTest(String url) throws InterruptedException {
        var es = Executors.newFixedThreadPool(100);
        var rt = new RestTemplate();

        var mainSw = new StopWatch();
        mainSw.start();

        for (int i = 0; i < 100; i++) {
            es.execute(() -> {
                var innerSw = new StopWatch();
                innerSw.start();
                rt.getForObject(url, String.class);
                innerSw.stop();
                log.info("Elapsed : {}", innerSw.getTotalTimeSeconds());
            });
        }

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        mainSw.stop();
        log.info("Total : {}", mainSw.getTotalTimeSeconds());
    }
}
