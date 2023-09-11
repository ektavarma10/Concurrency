
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Forks {

    List<Semaphore> forks;
    public Semaphore getFork(int n) {
        return forks.get(n);
    }

    Forks(int n) {
        forks = new ArrayList<>();

        for(int i=0;i<n;i++) {
            forks.add(new Semaphore(1));
        }

    }
}
class Philosopher {

    private Semaphore leftFork;
    private Semaphore rightFork;

    private Lock mutex;

    public Philosopher(Semaphore leftFork, Semaphore rightFork, Lock mutex) {
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.mutex = mutex;
    }

    public void thinking() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void eating() throws InterruptedException {
        while(true) {
            mutex.lock();
            if(leftFork.availablePermits()==1 && rightFork.availablePermits()==1) {
                acquireForks();

                System.out.println("Philosopher "+Thread.currentThread().getName()+" is eating");
                Thread.sleep(1000);
                System.out.println("Philosopher "+Thread.currentThread().getName()+" has finished eating");

                releaseForks();
                mutex.unlock();
                break;
            }
            mutex.unlock();
        }
    }

    private void releaseForks() {
        leftFork.release();
        rightFork.release();
    }

    private void acquireForks() throws InterruptedException {
        leftFork.acquire();
        rightFork.acquire();
    }
}
public class DiningPhilosopherProblem {
    public static void main(String[] args) {

        int numberOfPhilosopher = 5;

        Forks forks = new Forks(numberOfPhilosopher);

        List<Philosopher> philosophers = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        Lock mutex = new ReentrantLock();


        for (int i=0;i<numberOfPhilosopher;i++) {

            Philosopher philosopher = getPhilosopher(i, forks, numberOfPhilosopher, mutex);

            philosophers.add(philosopher);
        }


        for (Philosopher philosopher: philosophers) {
            Thread thread = new Thread(()->{
                philosopher.thinking();
                try {
                    philosopher.eating();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
            threads.add(thread);
        }

        terminateThreads(threads);

    }

    private static Philosopher getPhilosopher(int i, Forks forks, int numberOfPhilosopher, Lock mutex) {
        Semaphore leftFork = i ==0 ? forks.getFork(numberOfPhilosopher -1) : forks.getFork(i -1);
        Semaphore rightFork = forks.getFork(i);
        return new Philosopher(leftFork, rightFork, mutex);
    }

    private static void terminateThreads(List<Thread> threads) {
        for (Thread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
