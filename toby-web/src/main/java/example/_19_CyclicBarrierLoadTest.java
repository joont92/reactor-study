package example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class _19_CyclicBarrierLoadTest {
    public static void main(String[] args) throws Exception {
        loadTest();
    }

    private static void loadTest() throws InterruptedException, BrokenBarrierException {
        var es = Executors.newFixedThreadPool(100);
        var rt = new RestTemplate();

        var barrier = new CyclicBarrier(101);

        var atomic = new AtomicInteger(0);
        for (int i = 0; i < 100; i ++) {
            es.submit(() -> {
                var j = atomic.getAndIncrement();
                barrier.await();

                var innerSw = new StopWatch();
                innerSw.start();

                try {
//                var res = rt.getForObject("http://localhost:8080/deferred-rest?idx={idx}", String.class, j);
                    var res = rt.getForObject("http://localhost:8080/rest?idx={idx}", String.class, j);
                    innerSw.stop();
                    log.info("Elapsed {} : {}", innerSw.getTotalTimeSeconds(), res);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            });
        }

        barrier.await();

        var mainSw = new StopWatch();
        mainSw.start();

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        mainSw.stop();
        log.info("Total : {}", mainSw.getTotalTimeSeconds());
    }
}
