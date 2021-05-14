package toby;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;

/**
 * 연결된 observer 들에게 한번에 데이터를 push 할 수 있는 나름 강력한 패턴
 */
public class _05_ObserverExample {
    public static void main(String[] args) {
        var ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };

        var io = new IntObservable();
        io.addObserver(ob);

        var es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + " EXIT");
        es.shutdown();
    }

    static class IntObservable extends Observable implements Runnable {
        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                setChanged();
                notifyObservers(i);
            }
        }
    }
}
