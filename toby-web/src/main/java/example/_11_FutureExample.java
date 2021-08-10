package example;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Slf4j
public class _11_FutureExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var start = System.currentTimeMillis();
        var es = Executors.newCachedThreadPool();

        // 만약 Thread.sleep이 아니였다면 더 오랜 시간이 걸렸을 것이다
        // Thread.sleep은 cpu를 사용하지 않고 waiting 상태로 빠지기 때문이다
        var future = es.submit(() -> {
            // 계속 계산하는 로직이었다면, 이 쓰레드가 다른 cpu에 할당되지 않는다면 결과는 똑같을 듯..
//            for (long i = 0; i < 1000000000L; i++) { }
            Thread.sleep(2000);
            return "Hello";
        });

        // takes 4-500 ms
        for (long i = 0; i < 1000000000L; i++) { }

        future.get(); // blocking
        var end = System.currentTimeMillis();
        log.info(String.valueOf(end - start)); // 2010ms 정도 소요(2500ms 보다 덜 걸림)
        es.shutdown();
    }
}
