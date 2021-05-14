package toby;

import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class _02_MapOperatorExample {
    public static void main(String[] args) {
        var pub1 = iterPub(Stream.iterate(1, i -> i + 1).limit(10).collect(Collectors.toList()));
        var mapPub1_1 = mapPub(pub1, i -> i * 10);
        var mapPub1_2 = mapPub(mapPub1_1, i -> -i);
        mapPub1_2.subscribe(logSub());

        var pub2 = iterPub(Stream.iterate(1, i -> i + 1).limit(10).collect(Collectors.toList()));
        var mapPub2_1 = mapPub(pub2, i -> "[" + i + "]");
        mapPub2_1.subscribe(logSub());
    }

    // T를 받아서 R로 변환해주는 Operator
    // 여기 전달하는 Subscriber에게는 R 타입을 돌려주겠다는 의미
    private static <T, R> Flow.Publisher<R> mapPub(Flow.Publisher<T> pub, Function<T, R> function) {
        // iterPub.subscribe를 실행하면서 새로 생성한 Subscriber를 던짐
        // pub.subscribe(new Subscriber(subscriber))
        return s -> pub.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                s.onSubscribe(subscription);
            }

            @Override
            public void onNext(T item) {
                s.onNext(function.apply(item));
            }

            @Override
            public void onError(Throwable throwable) {
                s.onError(throwable);
            }

            @Override
            public void onComplete() {
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
