package toby;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IntervalEx {
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
                log.debug("onSubscribe");
                s = subscription;
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
                if (count++ > 10) {
                    s.cancel();
                    return;
                }
                log.debug("onNext {}", item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.debug("onError {}", throwable);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        };

        pub.subscribe(sub);
    }
}
