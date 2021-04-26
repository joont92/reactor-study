package toby;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class FluxSchedulerEx {
    public static void main(String[] args) {
//        pubsubOn();
        interval();
    }

    private static void pubsubOn() {
        Flux.range(1, 10)
                .publishOn(Schedulers.newSingle("pub")) // consumer가 느릴 경우 사용
                .log()
                .subscribeOn(Schedulers.newSingle("sub")) // publisher가 느릴 경우 사용
                .subscribe(System.out::println);
        System.out.println("exit");
    }

    private static void interval() {
        Flux.interval(Duration.ofMillis(200))
                .take(10)
                .subscribe(System.out::println);

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
