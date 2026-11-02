public class Patient implements Runnable {
    private final int patientId;
    private final WaitingRoom waitingRoom;
    private boolean treated;

    public Patient(int patientId, WaitingRoom waitingRoom) {
        this.patientId = patientId;
        this.waitingRoom = waitingRoom;
        this.treated = false;
    }

    @Override
    public void run() {
        try {
            SimulationLogger.log("Patient " + patientId + " arrived at the hospital.");
            
            waitingRoom.enter(this);

            synchronized (this) {
                while (!treated) {
                    this.wait();
                }
            }

            SimulationLogger.log("Patient " + patientId + " left the hospital after treatment.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            SimulationLogger.log("Patient " + patientId + " was interrupted.");
        }
    }

    public int getPatientId() {
        return patientId;
    }

    public synchronized void markAsTreated() {
        this.treated = true;
        this.notifyAll();
    }

    public synchronized boolean isTreated() {
        return treated;
    }
}
