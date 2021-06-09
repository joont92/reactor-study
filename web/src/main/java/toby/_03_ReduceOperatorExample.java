package toby;

import java.util.concurrent.Flow;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class _03_ReduceOperatorExample {
    public static void main(String[] args) {
        var pub1 = iterPub(Stream.iterate(1, i -> i + 1).limit(10).collect(Collectors.toList()));
        var reducePub1 = reducePub(pub1, 0, (a, b) -> a + b);
        reducePub1.subscribe(logSub());

        var pub2 = iterPub(Stream.iterate(1, i -> i + 1).limit(10).collect(Collectors.toList()));
        var reducePub2 = reducePub(pub2, "", (a, b) -> a + "-" + b);
        reducePub2.subscribe(logSub());
    }

    private static <T, R> Flow.Publisher<R> reducePub(
            Flow.Publisher<T> pub, R init, BiFunction<R, T, R> function) {
        return s -> pub.subscribe(new Flow.Subscriber<>() {
            R result = init;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                s.onSubscribe(subscription);
            }

            @Override
            public void onNext(T item) {
                result = function.apply(result, item);
            }

            @Override
            public void onError(Throwable throwable) {
                s.onError(throwable);
            }

            @Override
            public void onComplete() {
                s.onNext(result);
                s.onComplete();
            }
        });
    }

    private static Flow.Publisher<Integer> iterPub(Iterable<Integer> itr) {
        return s -> s.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(long n) {
                for (Integer i : itr) {
                    s.onNext(i);
                }
                s.onComplete();
            }

            @Override
            public void cancel() {

            }
        });
    }

    private static <T> Flow.Subscriber<T> logSub() {
        return new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                System.out.println("on subscribe");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
                System.out.println("on next " + item);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("on error");
            }

            @Override
            public void onComplete() {
                System.out.println("on complete");
            }
        };
    }
}
