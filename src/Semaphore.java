public class Semaphore{
    private int value = 0;
    public Semaphore(){
        value = 0;
    }
    public Semaphore(int initial){
        value = initial;
    }
    public synchronized void acquire(){
        while(value == 0){
            try{
                wait();
            }
            catch(InterruptedException e){
                System.out.println("Exception Error: " + e);
            }
        }
        value--;
    } 
    public synchronized void release(){
        value++;
        notifyAll();
    }

    public synchronized boolean acq_timeout(long timeout) {
        
        long old_time = System.currentTimeMillis();

        while(value == 0){
            try{
                long cur_time = System.currentTimeMillis();
                long time = old_time + timeout - cur_time;
                if(time <= 0) return false;
                wait(time);
            }catch(InterruptedException e){
                
            }
        }
        value--;
        return true;

        
    }

    public synchronized int get_value(){
        return value;
    }
    
}
