package toby;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

@Slf4j
public class _10_SchedulerExample {
    public static void main(String[] args) {

        Flow.Publisher<Integer> pub = sub -> sub.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(long n) {
                log.info("request");
                sub.onNext(1);
                sub.onNext(2);
                sub.onNext(3);
                sub.onNext(4);
                sub.onNext(5);
                sub.onComplete();
            }

            @Override
            public void cancel() {

            }
        });

        // pub + sub 다른 쓰레드에서 처리
        Flow.Publisher<Integer> subOnPub = sub -> {
            var es = Executors.newFixedThreadPool(100);
            es.execute(() -> pub.subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    sub.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer item) {
                    sub.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    sub.onError(throwable);
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    sub.onComplete();
                    es.shutdown();
                }
            }));
        };

        // sub만 다른 쓰레드에서 처리
        Flow.Publisher<Integer> pubOnPub = sub -> subOnPub.subscribe(new Flow.Subscriber<>() {
            // 멀티 쓰레드로 처리하면 안된다
            ExecutorService es = Executors.newSingleThreadExecutor();

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                sub.onSubscribe(subscription);
            }

            @Override
            public void onNext(Integer item) {
                es.execute(() -> sub.onNext(item));
            }

            @Override
            public void onError(Throwable throwable) {
                es.execute(() -> sub.onError(throwable));
                es.shutdown();
            }

            @Override
            public void onComplete() {
                es.execute(sub::onComplete);
                es.shutdown();
            }
        });

        pubOnPub.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                log.info("onSubscribe");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
                log.info("onNext {}", item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.info("onError {}", throwable.getMessage());
            }

            @Override
            public void onComplete() {
                log.info("onComplete");
            }
        });

        log.info("EXIT");
    }
}
