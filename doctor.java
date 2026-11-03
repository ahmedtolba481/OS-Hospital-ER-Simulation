import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Doctor extends Thread {

    private int doctorId;
    private WaitingRoom waitingRoom;
    private TreatmentRoomManager treatmentRoomManager;
    private CountDownLatch completionLatch;
    private Random random;

    public Doctor(int doctorId, WaitingRoom waitingRoom, TreatmentRoomManager treatmentRoomManager,CountDownLatch completionLatch) {
        this.doctorId = doctorId;
        this.waitingRoom = waitingRoom;
        this.treatmentRoomManager = treatmentRoomManager;
        this.completionLatch = completionLatch;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Patient patient = waitingRoom.takePatient();
                if (patient == null) {
                 break;
                }
                System.out.println("Doctor " + doctorId+ " is assigned to P" + patient.getPatientId() + );
                System.out.println("Doctor " + doctorId+ " is trying to acquire a treatment room.");

                boolean acquired = treatmentRoomManager.acquireRoom(3000);
                if (!acquired) {
                    System.out.println("Doctor " + doctorId+ " could not acquire a treatment room.");
                    System.out.println("Doctor " + doctorId+ " is sending P" + patient.getPatientId()+ " back to the waiting room.");

                    returnPatientAsync(patient);
                    continue;
                }
                treatPatient(patient);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void treatPatient(Patient patient) throws InterruptedException {
        try {
            System.out.println("Doctor " + doctorId + " acquired a treatment room.");
            System.out.println("Doctor " + doctorId+ " begins treating P" + patient.getPatientId() + ".");
            Thread.sleep(random.nextInt(2000) + 2000);
            System.out.println("Doctor " + doctorId + " finished treating P" + patient.getPatientId() + ".");
        } finally {
            treatmentRoomManager.releaseRoom();
            System.out.println("Doctor " + doctorId+ " released the treatment room.");
        }
        patient.markAsTreated();
        System.out.println("Doctor " + doctorId + " is now available.");
        System.out.println("P" + patient.getPatientId() + " left.");
        completionLatch.countDown();
    }

  
    private void returnPatientAsync(Patient patient) {
        new Thread(() -> {try {waitingRoom.returnPatient(patient);
            } catch (InterruptedException e) {Thread.currentThread().interrupt();}
        }).start();
    }
}
