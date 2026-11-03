public class TreatmentRoomManager{
    private Semaphore trooms;
    public TreatmentRoomManager(int n_rooms){
        this.trooms = new Semaphore(n_rooms);
    }
    public boolean acquireRoom (int timeout)  throws InterruptedException{
        return trooms.acq_timeout(timeout);
    }
    public void releaseRoom(){
        trooms.release();
    }
}