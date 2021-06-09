package reactor;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Slf4j
public class FluxEx {
    public static void main(String[] args) {
//        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4); // (1)
//
//        // map이 더해지는 과정은, 위에서부터 아래로 publisher가 추가된다고 보면 된다
//        final Flux<String> flux = Flux.range(1, 2)
//                .map(i -> 10 + i) // (2)
//                .log()
//                .publishOn(s) // (3)
//                .map(i -> "value " + i) // (4)
//                .log();
//
//        new Thread(() -> flux.subscribe(System.out::println)).start(); // (5)

//        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4); // (1)
//        // 아래로 내려가면 publisher가 하나씩 더 붙는다고 생각하면 됨
//        final Flux<String> flux = Flux.range(1, 2)
//                .map(i -> 10 + i) // (2)
//                .log()
//                .subscribeOn(s) // (3)
//                .map(i -> "value " + i)
//                .log(); // (4)
//
//        new Thread(() -> flux.subscribe(System.out::println)).start(); // (5)
        generate1();
    }

    private static void generate1() {
        // Consumer<SynchronousSink>로 subscriber에게 전달할 데이터를 계속 생성함
        Flux.<Integer>generate(sync -> {
            var number = new Random().nextInt();
            log.info("going to emit - {}", number);
            sync.next(number);
        }).delayElements(Duration.ofMillis(10)).subscribe(s -> log.info("onNext : {}", s));

        try { Thread.sleep(1000); } catch (Exception e) { }
    }

    private static void generate1_1() {
        Flux<Integer> integerFlux = Flux.generate(f -> IntStream.range(0, 5)
                .peek(i -> log.info("going to emit - {}", i))
                .forEach(f::next));
        integerFlux.delayElements(Duration.ofMillis(1)).subscribe(i -> log.info("First {}", i));
//        integerFlux.delayElements(Duration.ofMillis(2)).subscribe(i -> log.info("Second {}", i));
        try { Thread.sleep(1000); } catch (Exception e) { }
    }

    private static void generate1_2() {
        AtomicInteger atomicInteger = new AtomicInteger();

        //Flux generate sequence
        Flux<Integer> integerFlux = Flux.generate(s -> {
            System.out.println("Flux generate");
            s.next(atomicInteger.getAndIncrement());
        });

        //observer
        integerFlux.delayElements(Duration.ofMillis(100))
                .subscribe(i -> System.out.println("First consumed ::" + i));

        try { Thread.sleep(10000); } catch (Exception e) { }
    }

    private static void generate2() {
        Flux.<Integer, Integer>generate(
                () -> 1,
                (state, sync) -> {
                    sync.next(state);
                    if (state == 10) {
                        sync.complete();
                    }
                    return state + 1;
                })
        .subscribe(s -> log.info("onNext : {}", s));
    }

    private static void generate3() {
        Flux.<String, AtomicLong>generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                }, (state) -> System.out.println("state: " + state))
        .subscribe(s -> System.out.println("onNext : " + s));
    }

    // pull
    private static void create1() {
        Flux<Integer> integerFlux = Flux.create(f -> IntStream.range(0, 30)
                .peek(i -> log.info("going to emit - {}", i))
                .forEach(f::next));
//        integerFlux.delayElements(Duration.ofMillis(1)).subscribe(i -> log.info("First {}", i));
        integerFlux.delayElements(Duration.ofMillis(2)).subscribe(i -> {
            try { Thread.sleep(new Random().nextInt() % 1000); } catch (Exception e) { /**/ }
            log.info("Second {}", i);
        });
        try { Thread.sleep(100000); } catch (Exception e) { }
    }

    private static void create1_1() {
        Flux<Integer> integerFlux = Flux.create(f -> {
            log.info("Flux create");
            IntStream.range(0, 100)
                    .peek(i -> log.info("going to emit - {}", i))
                    .forEach(f::next);
        }, FluxSink.OverflowStrategy.BUFFER);
        integerFlux.delayElements(Duration.ofMillis(50))
                .subscribe(i -> {
                    log.info("First consumed :: {}", i);
                });

        try { Thread.sleep(1000); } catch (Exception e) { }
    }

    // filter + map
    private static void handle() {
        Flux.range(1, 10)
                .handle((i, sync) -> {
                   if (i % 2 == 0) {
                       sync.next(i + "rd");
                   }
                })
        .subscribe(s -> log.info("onNext : {}", s));
    }
}
