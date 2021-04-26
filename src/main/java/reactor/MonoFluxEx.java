package reactor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class MonoFluxEx {
    public static void main(String[] args) {
        var just = Flux.just(1,2,3,4,5);
        var range = Flux.range(1, 10);
        var fromArray = Flux.fromArray(new Integer[]{1,2,3,4,5});
        just.subscribe(i -> log.debug(String.valueOf(i)));
        System.out.println();
        range.subscribe(i -> log.debug(String.valueOf(i)));
        System.out.println();
        fromArray.subscribe(i -> log.debug(String.valueOf(i)));
        System.out.println();

        var ints = Flux.range(1, 5)
                .map(i -> i * 10);
        ints.subscribe(i -> log.debug(String.valueOf(i)));
        System.out.println();

        var intsWithError = Flux.range(1, 5)
                .map(i -> {
                    if (i > 3) {
                        throw new IllegalArgumentException();
                    }
                    return i;
                });
        intsWithError.subscribe(i -> log.debug(String.valueOf(i)), t -> log.error("{}", t));
        System.out.println();

        ints.subscribe(
                i -> log.debug(String.valueOf(i)),
                Throwable::printStackTrace,
                () -> log.debug("COMPLETE!"),
                s -> s.request(2));
        System.out.println();

        var mono = Mono.just("test");
        mono.subscribe(log::debug);
    }
}
