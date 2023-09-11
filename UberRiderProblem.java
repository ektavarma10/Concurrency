import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class UberRide {
    private Semaphore republicans;
    private Semaphore democrats;

    private Lock mutex;

    private int capacity;

    public UberRide(int n) {
        this.capacity = n;
        this.mutex = new ReentrantLock();
        this.republicans = new Semaphore(n);
        this.democrats = new Semaphore(n);
    }

    public void seatDemocrat() throws InterruptedException {
        democrats.acquire();
        drive();
        System.out.println("Seated Democrat "+ Thread.currentThread().getName()+"; Available Permits "+ democrats.availablePermits());
    }

    public void seatRepublican() throws InterruptedException {
        republicans.acquire();
        drive();
        System.out.println("Seated Republican "+ Thread.currentThread().getName()+"; Available Permits "+ republicans.availablePermits());
    }

    public void drive() {
        while(true) {
            mutex.lock();
            if (republicans.availablePermits() == 0) {
                System.out.println("Uber Ride started with 4 republicans");
                republicans.release(capacity);
            }
            if (democrats.availablePermits() == 0) {
                System.out.println("Uber Ride started with 4 democrats");
                democrats.release(capacity);
            }
            if (democrats.availablePermits() <= 2 && republicans.availablePermits() <= 2) {

                System.out.println("Uber Ride started with 2 democrats and 2 republicans");
                democrats.release(2);
                republicans.release(2);
            }
            mutex.unlock();
        }
    }
}
public class UberRiderProblem {
    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();

        int capacity = 4;
        int numberOfPassenger = 4;

        UberRide uberRide = new UberRide(capacity);

        for(int i=0;i<numberOfPassenger;i++) {
            Thread democrats = getRideForDemocrats(uberRide);
            threads.add(democrats);
        }

        for(int i=0;i<numberOfPassenger;i++) {
            Thread republican = getRideForRepublican(uberRide);
            threads.add(republican);
        }

        terminateThreads(threads);
    }

    private static Thread getRideForDemocrats(UberRide uberRide) {
        Thread democrats = new Thread(()-> {
            try {
                uberRide.seatDemocrat();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        democrats.start();
        return democrats;
    }

    private static Thread getRideForRepublican(UberRide uberRide) {
        Thread republican = new Thread(()-> {
            try {
                uberRide.seatRepublican();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        republican.start();
        return republican;
    }

    private static void terminateThreads(List<Thread> threads) {
        for(Thread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
