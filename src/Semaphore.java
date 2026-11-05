public class Semaphore{
    private int value = 0;
    public Semaphore(){
        value = 0;
    }
    public Semaphore(int initial){
        if (initial < 0) {
            throw new IllegalArgumentException(
                    "Semaphore permits cannot be negative"
            );
        }

        value = initial;
    }
    public synchronized void acquire() throws InterruptedException{
        while(value == 0){        
            wait();

        }
        value--;
    } 
    public synchronized void release(){
        value++;
        notifyAll();
    }

    public synchronized boolean acq_timeout(long timeout)  throws InterruptedException{
        
        long old_time = System.currentTimeMillis();

        while(value == 0){
            long cur_time = System.currentTimeMillis();
            long time = old_time + timeout - cur_time;
            if(time <= 0) return false;
            wait(time);

        }
        value--;
        return true;

        
    }

    public synchronized int get_value(){
        return value;
    }
    
}
