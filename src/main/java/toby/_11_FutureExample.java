package toby;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Slf4j
public class _11_FutureExample {
    // 2250~ 보다 덜 걸림
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var start = System.currentTimeMillis();
        var es = Executors.newCachedThreadPool();

        var future = es.submit(() -> {
            Thread.sleep(2000);
            return "Hello";
        });

        // takes 260-280 ms
        for (long i = 0; i < 1000000000L; i++) { }

        future.get(); // blocking
        var end = System.currentTimeMillis();
        log.info(String.valueOf(end - start));
    }
}
