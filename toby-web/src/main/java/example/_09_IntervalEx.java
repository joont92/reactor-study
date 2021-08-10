package example;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

@Slf4j
public class _09_IntervalEx {
    public static void main(String[] args) {
        Flow.Publisher<Integer> pub = sub -> sub.onSubscribe(new Flow.Subscription() {
            boolean canceled = false;
            int num = 1;
            @Override
            public void request(long n) {
                var es = Executors.newSingleThreadScheduledExecutor();
                es.scheduleAtFixedRate(() -> {
                    if (canceled) {
                        es.shutdown();
                        return;
                    }
                    sub.onNext(num++);
                }, 0, 500, TimeUnit.MILLISECONDS);
            }

            @Override
            public void cancel() {
                canceled = true;
            }
        });

        Flow.Subscriber<Integer> sub = new Flow.Subscriber<>() {
            Flow.Subscription s;
            int count = 1;
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                log.info("onSubscribe");
                s = subscription;
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
                if (count++ > 10) {
                    s.cancel();
                    return;
                }
                log.info("onNext {}", item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.info("onError {}", throwable);
            }

            @Override
            public void onComplete() {
                log.info("onComplete");
            }
        };

        pub.subscribe(sub);
    }
}
