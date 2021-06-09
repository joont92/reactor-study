package toby;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

// 비동기 코드와 비즈니스 로직 코드가 섞여있는 것이 마음에 들지 않는다!
@Slf4j
public class _12_CallbackFutureExample {
    interface SuccessCallback {
        void onSuccess(String result);
    }
    interface ExceptionCallback {
        void onError(Throwable throwable);
    }

    public static class CallbackFutureTask extends FutureTask<String> {
        private SuccessCallback sc;
        private ExceptionCallback ec;
        public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
            super(callable);
            this.sc = Objects.requireNonNull(sc);
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            try {
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // interrupt exception은 예외를 받는게 중요한게 아니라, 인터럽트가 일어났다는 시그널을 주는것이 중요하
            } catch (ExecutionException e) { // 비동기 작업 내에서 Exception이 발생했을때
                ec.onError(e.getCause());
            }
        }
    }

    public static void main(String[] args) {
        var es = Executors.newCachedThreadPool();
        var future = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            if (1 == 1) {
                throw new NullPointerException();
            }
            return "Hello";
        }, log::info, t -> log.error("Error : {}", t.getMessage()));

        es.execute(future);
        es.shutdown();
    }
}
