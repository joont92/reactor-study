package reactor;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BaseSubscriberEx {
    public static void main(String[] args) {
        var publisher = Flux.range(1, 5).log();
        var subscriber = new SampleSubscriber();
        publisher.subscribe(subscriber);
        publisher.subscribe(subscriber); // cancel 호출됨

        publisher.buffer(10)
                .subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        log.info("onSubscribe");
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(List<Integer> integers) {
                        log.info("onNext " + integers.stream().map(String::valueOf).collect(Collectors.joining(", ")));
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.info("onError");
                    }

                    @Override
                    public void onComplete() {
                        log.info("onComplete");
                    }
                });
    }

    public static class SampleSubscriber extends BaseSubscriber<Integer> {
        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            log.info("onSubscribe");
            request(1);
//            requestUnbounded();
//            cancel();
        }

        @Override
        protected void hookOnNext(Integer value) {
            log.info("onNext " + value);
            request(1);
        }

        @Override
        protected void hookOnComplete() {
            log.info("onComplete");
        }

        @Override
        protected void hookFinally(SignalType type) {
            log.info("onFinally {}", type);
        }
    }
}
