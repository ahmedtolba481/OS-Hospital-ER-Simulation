import java.util.LinkedList;
import java.util.Queue;

public class WaitingRoom {
    private final Queue<Patient> patients;
    private final int capacity;

    private final Semaphore emptySlots;
    private final Semaphore availablePatients;
    private final Semaphore mutex;

    private boolean closed;

    public WaitingRoom(int capacity) {
        this.capacity = capacity;
        this.patients = new LinkedList<>();
        this.emptySlots = new Semaphore(capacity);
        this.availablePatients = new Semaphore(0);
        this.mutex = new Semaphore(1);
        this.closed = false;
    }

    public void enter(Patient patient) throws InterruptedException {
        emptySlots.acquire();

        mutex.acquire();
        try {
            patients.add(patient);
            SimulationLogger.log("Patient " + patient.getPatientId() + " entered the waiting room.");
        } finally {
            mutex.release();
        }

        availablePatients.release();
    }

    public Patient takePatient() throws InterruptedException {
        availablePatients.acquire();

        mutex.acquire();
        try {
            if (closed && patients.isEmpty()) {
                return null;
            }

            Patient patient = patients.poll();
            SimulationLogger.log("Doctor took Patient " + (patient != null ? patient.getPatientId() : "null") + " from waiting room.");
            return patient;
        } finally {
            mutex.release();
        }
    }

    public void returnPatient(Patient patient) throws InterruptedException {
        mutex.acquire();
        try {
            ((LinkedList<Patient>) patients).addFirst(patient);
            SimulationLogger.log("Doctor returned Patient " + patient.getPatientId() + " back to the waiting room due to timeout.");
        } finally {
            mutex.release();
        }

        availablePatients.release();
    }

    public void close(int numberOfDoctors) {
        try {
            mutex.acquire();
            this.closed = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }

        for (int i = 0; i < numberOfDoctors; i++) {
            availablePatients.release();
        }
    }

    public void releaseSeat() {
        emptySlots.release();
    }
}
