import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ReaderWriter {

    private int readerCount;

    private Lock readLock;

    private Semaphore writeLock;

    public int getReaderCount() {
        return readerCount;
    }

    public ReaderWriter() {

        this.readerCount=0;

        this.readLock = new ReentrantLock();
        this.writeLock = new Semaphore(1);
    }

    public void acquireReadLock() throws InterruptedException {
        readLock.lock();

        if(readerCount==0){
            System.out.println("Acquire ReadLock: Locking write lock");
            writeLock.acquire();
        }

        readerCount++;
        readLock.unlock();
    }

    public void acquireWriteLock() throws InterruptedException {

        while(true) {
            readLock.lock();
            if (readerCount==0) {
                writeLock.acquire();
                readLock.unlock();
                break;
            }
            readLock.unlock();
        }

    }

    public void releaseReadLock() {
        readLock.lock();
        readerCount--;
        if(readerCount==0 && writeLock.availablePermits()==0) {
            System.out.println("Realease ReadLock: Unlocked write lock");
            writeLock.release();
        }
        readLock.unlock();
    }

    public void releaseWriteLock() {
        writeLock.release();
    }


}
public class ReaderWriterProblem {


    public static void main(String[] args) {
        int numOfWriters = 5;
        int numOfReaders = 5;

        ReaderWriter readerWriter = new ReaderWriter();

        ArrayList<Thread> threads = new ArrayList<>();

        for(int i=0;i<numOfReaders;i++) {
            Thread readerThread = getReaderThread(readerWriter);
            threads.add(readerThread);
        }

        for(int i=0;i<numOfWriters;i++) {
            Thread writerThread = getWriterThread(readerWriter);
            threads.add(writerThread);
        }

        for(Thread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("All threads have terminated successfully");
    }

    private static Thread getWriterThread(ReaderWriter readerWriter) {
        Thread writerThread = new Thread(()-> {
            try {
                readerWriter.acquireWriteLock();
                System.out.println("Reader Count: "+ readerWriter.getReaderCount());
                System.out.println("Writer "+Thread.currentThread().getName()+ " is writing");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            } finally {
                readerWriter.releaseWriteLock();
                System.out.println("Writer "+Thread.currentThread().getName()+ " has finished writing");
            }

        });
        writerThread.start();
        return writerThread;
    }

    private static Thread getReaderThread(ReaderWriter readerWriter) {
        Thread readerThread = new Thread(() -> {
            try {
                readerWriter.acquireReadLock();
                System.out.println("Reader "+Thread.currentThread().getName()+ " is reading; " +
                        "ReaderCount: "+ readerWriter.getReaderCount());
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                readerWriter.releaseReadLock();
                System.out.println("Reader "+Thread.currentThread().getName()+ " has finished reading; " +
                        "ReaderCount: "+ readerWriter.getReaderCount());
            }
        });
        readerThread.start();
        return readerThread;
    }
}
