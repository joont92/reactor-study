package reactor;

import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

public class DisposableEx {
    public static void main(String[] args) {
        var integer = new AtomicInteger(1);
        var flux = Flux.generate(o -> {
            o.next(integer.getAndIncrement());
            try { Thread.sleep(100); } catch (Exception e) { /* */ }
        });
        var disposable = flux.subscribe(System.out::println);
        disposable.dispose();
    }
}
