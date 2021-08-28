import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest {
    private final StopWatch sw = new StopWatch();
    private final Logger log = LoggerFactory.getLogger(CompletableFutureTest.class);
    private final ExecutorService cachedEs = Executors.newCachedThreadPool();
    private final ExecutorService singleEs = Executors.newSingleThreadExecutor();

    @Test
    public void 비동기_가격조회1() {
        CompletableFuture<Integer> future1 = getPriceAsync1();
        log.info("future called");
        future1.join();
        log.info("future end");

        CompletableFuture<Integer> future2 = getPriceAsync2();
        log.info("future called");
        future2.join();
        log.info("future end");
    }

    @Test
    public void thenAccept() {
        CompletableFuture<Void> future = getPriceAsync2()
            .thenAccept(it -> log.info("then accept: {}", it));
        log.info("future called");
        log.info("future end: {}", future.join());
    }

    @Test
    public void thenApply() {
        CompletableFuture<Integer> future = getPriceAsync2()
            .thenApply(it -> {
                log.info("then apply: {}", it);
                return it + 1;
            });
        log.info("future called");
        log.info("future end: {}", future.join());
    }

    @Test
    public void thenAcceptApplyAsync() {
        CompletableFuture<Void> future = getPriceAsync2()
            .thenApplyAsync(it -> {
                log.info("then apply: {}", it);
                return it + 1;
            })
            .thenAcceptAsync(it -> {
                log.info("then accept: {}", it);
            });
        log.info("future called");
        log.info("future end: {}", future.join());
    }

    @Test
    public void thenAcceptApplyAsyncWithExecutor() {
        CompletableFuture<Void> future = getPriceAsync2()
            .thenApplyAsync(it -> {
                log.info("then apply: {}", it);
                return it + 1;
            }, cachedEs)
            .thenAcceptAsync(it -> {
                log.info("then accept: {}", it);
            }, cachedEs);
        log.info("future called");
        log.info("future end: {}", future.join());
    }

    @Test
    public void thenCombine() {
        sw.start();
        CompletableFuture<Integer> future1 = getPriceAsync2();
        CompletableFuture<Integer> future2 = getPriceAsync2();

        CompletableFuture<Integer> future = future1.thenCombine(future2, Integer::sum);
        log.info("future end: {}", future.join()); // 2초밖에 안걸림
        sw.stop();

        log.info("ms: {}", sw.getTotalTimeMillis());
    }

    @Test
    public void thenCombineSingleThread() {
        sw.start();
        CompletableFuture<Integer> future1 = getPriceAsync2(singleEs);
        CompletableFuture<Integer> future2 = getPriceAsync2(singleEs);

        CompletableFuture<Integer> future = future1.thenCombine(future2, Integer::sum);
        log.info("future end: {}", future.join()); // 4초 걸림
        sw.stop();

        log.info("ms: {}", sw.getTotalTimeMillis());
    }

    @Test
    public void allOf() {
        sw.start();
        CompletableFuture<Integer> future1 = getPriceAsync2();
        CompletableFuture<Integer> future2 = getPriceAsync2();
        CompletableFuture<Integer> future3 = getPriceAsync2();

        CompletableFuture<List<Integer>> future = CompletableFuture.allOf(future1, future2, future3)
            .thenApply(_void -> Arrays.asList(future1.join(), future2.join(), future3.join()));
        log.info("future end: {}", future.join().stream().mapToInt(Integer::intValue).sum()); // 3개를 병렬로 실행했으므로 2초 걸림
        sw.stop();

        log.info("ms: {}", sw.getTotalTimeMillis());
    }

    private CompletableFuture<Integer> getPriceAsync1() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        cachedEs.execute(() -> {
            log.info("getPriceAsync in");
            future.complete(getPrice());
        });
        return future;
    }

    private CompletableFuture<Integer> getPriceAsync2(ExecutorService es) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("getPriceAsync2 in");
            return getPrice();
        }, es);
    }
    private CompletableFuture<Integer> getPriceAsync2() {
        return getPriceAsync2(cachedEs);
    }

    private Integer getPrice() {
        try {
            Thread.sleep(2000);
        } catch (Exception ignore) { }
        return 10;
    }
}


