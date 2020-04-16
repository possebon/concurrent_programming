import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;

public class ListPerformanceAnalysis {
    private static int TAMANHO_LISTA;
    private static int TAMANHO_MAXIMO;
    private static int QTDE_THREADS;
    private static int TEMPO_WARMUP;
    private static int TEMPO_EXECUCAO;

    private static LockedQueue<Integer> queue;

    public static void main(String[] args) throws InterruptedException {

        if (args.length != 5) {

            System.out.println("Favor informar todos os argumentos nesta ordem: TAMANHO_LISTA TAMANHO_MAXIMO QTDE_THREADS TEMPO_WARMUP TEMPO_EXECUCAO");

        }
        else
        {
            TAMANHO_LISTA = Integer.parseInt(args[0]);
            TAMANHO_MAXIMO = Integer.parseInt(args[1]);
            QTDE_THREADS = Integer.parseInt(args[2]);
            TEMPO_WARMUP = Integer.parseInt(args[3]);
            TEMPO_EXECUCAO = Integer.parseInt(args[4]);
        }
        
        queue = new LockedQueue<>(TAMANHO_MAXIMO);
        
        ExecutorService executor = Executors.newFixedThreadPool(QTDE_THREADS);
        executor.execute(new ProducerTask());
        executor.execute(new ConsumerTask());
        executor.shutdown();

        
    }

    private static class ProducerTask implements Runnable {
        private static Long start_time = 0L;
        private static Long task_duration;
        private static Long executionTimes = 0L;

        public void run() {
            try {
                start_time = System.currentTimeMillis();
                for (int i = 0; i < TAMANHO_LISTA; i++) {
                    System.out.println("Producer writes: " + i);
                    //buffer.write(i++);
                    queue.put(i++);

                    Thread.sleep((int) (Math.random() * 1000));
                }
                executionTimes += 1;
                task_duration = System.currentTimeMillis() - start_time;
                System.out.println("ProducerTask duration: " + task_duration + " miliseconds");
                System.out.println("ProducerTask executed " + executionTimes + " times");
            } catch (InterruptedException ie) {
                System.out.println("Producer IE: " + ie.getMessage());
            }
        }
    }

    private static class ConsumerTask implements Runnable {
        private static Long start_time = 0L;
        private static Long task_duration;
        private static Long executionTimes = 0L;
        private volatile boolean killed = false;
        public void run() {
            
            try {
                start_time = System.currentTimeMillis();
                while (!killed) {

                        Integer value = queue.take();
                        System.out.println("Consumer reads: " + value);
                        
                        // buffer.write(i++);

                        // Thread.sleep((int) (Math.random() * 1000));
                        executionTimes += 1;
                        System.out.println("Executions: " + executionTimes);


  
                }
                
                // task_duration = System.currentTimeMillis() - start_time;
                // System.out.println("ConsumerTask duration: " + task_duration + " miliseconds");
                // System.out.println("ConsumerTask executed " + executionTimes + " times");
            } catch (InterruptedException ie) {
                System.out.println("Consumer IE: " + ie.getMessage());
            }
            {
                killed = true;
            }
        }
    }

}

class LockedQueue<T> {
    final Lock lock = new ReentrantLock();
    final Condition notFull = lock.newCondition();
    final Condition notEmpty = lock.newCondition();
    final T[] items;
    int tail, head, count;

    public LockedQueue(int queueSize) {
        items = (T[]) new Object[queueSize];

    }

    public void put(T x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();
            items[tail] = x;
            if (++tail == items.length)
                tail = 0;
            ++count;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();
            T x = items[head];
            if (++head == items.length)
                head = 0;
            --count;
            notFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }
}
