package example;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
public class _22_CompletableFuture {
    public static void main(String[] args) throws InterruptedException {
        CompletableFuture
                .supplyAsync(() -> {
                    log.info("supplyAsync");
                    return 1;
                })
                .thenCompose(s -> {
                    log.info("thenApply {}", s);
                    return CompletableFuture.completedFuture(s + 1);
                })
                .thenApply(s2 -> {
                    log.info("thenApply {}", s2);
                    return s2 * 3;
                })
                .exceptionally(e -> -10)
                .thenAccept(s3 -> log.info("thenAccept {}", s3));
        CompletableFuture
                .runAsync(() -> log.info("runAsync1"))
                .thenRun(() -> log.info("thenRun1"));
        log.info("EXIT");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }
}
