import java.util.Random;

public class Doctor extends Thread {
    private final int doctorId;
    private final WaitingRoom waitingRoom;
    private final TreatmentRoomManager treatmentRoomManager;
    private final Random random;
    public Doctor(int doctorId, WaitingRoom waitingRoom,
                  TreatmentRoomManager treatmentRoomManager
                  ) {
        this.doctorId = doctorId;
        this.waitingRoom = waitingRoom;
        this.treatmentRoomManager = treatmentRoomManager;
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
                Output.log("Doctor " + doctorId + " is assigned to P" + patient.getPatientId());
                Output.log("Doctor " + doctorId + " is trying to acquire a treatment room.");
                boolean acquired = treatmentRoomManager.acquireRoom((int) Pace.scale(3000));
                if (!acquired) {
                    Output.log("Doctor " + doctorId + " could not acquire a treatment room.");
                    waitingRoom.returnPatient(patient,doctorId);
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
            Output.log("Doctor " + doctorId + " acquired a treatment room.");
            Output.log("Doctor " + doctorId + " begins treating P" + patient.getPatientId() );
            Pace.sleep(random.nextInt(2000) + 2000);
            Output.log("Doctor " + doctorId + " finished treating P" + patient.getPatientId() );
        } finally {
            treatmentRoomManager.releaseRoom();
            Output.log("Doctor " + doctorId + " released the treatment room.");
        }
        patient.markAsTreated();
        Output.log("Doctor " + doctorId + " is now available.");
    }
}
