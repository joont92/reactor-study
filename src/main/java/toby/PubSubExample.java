package toby;

import java.util.Arrays;
import java.util.concurrent.Flow;

public class PubSubExample {
    public static void main(String[] args) {
        var publisher = new PublisherImpl();
        var subscriber = new SubscriberImpl();
        publisher.subscribe(subscriber);
    }

    static class PublisherImpl implements Flow.Publisher<Integer> {
        Iterable<Integer> itr = Arrays.asList(1,2,3,4,5);

        @Override
        public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
            var it = itr.iterator();

            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                    while (it.hasNext()) {
                        subscriber.onNext(it.next());
                    }

                    subscriber.onComplete();
                }

                @Override
                public void cancel() {

                }
            });
        }
    }

    static class SubscriberImpl implements Flow.Subscriber<Integer> {
        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            System.out.println("on subscribe");
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(Integer item) {
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
    }
}
