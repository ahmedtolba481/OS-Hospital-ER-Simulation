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
        Output.log("Patient " + patient.getPatientId()
                + " is waiting for an available seat.");
        emptySlots.acquire();
        mutex.acquire();
        try {
            patients.add(patient);
            Output.log("Patient " + patient.getPatientId()
                    + " entered the waiting room.");
        } finally {
            mutex.release();
        }

        availablePatients.release();
    }
    public Patient takePatient() throws InterruptedException {
        availablePatients.acquire();

        mutex.acquire();

        Patient patient = null;

        try {
            if (closed && patients.isEmpty()) {
                return null;
            }

            patient = patients.poll();

            Output.log("A Doctor took Patient "
                    + (patient != null ? patient.getPatientId() : "null")
                    + " from waiting room.");

        } finally {
            mutex.release();
        }

        if (patient != null) {
            emptySlots.release();
        }

        return patient;
    }
    public void returnPatient(Patient patient, int doctorId) throws InterruptedException {
        Output.log("Doctor " + doctorId + " is sending P"
                + patient.getPatientId() + " back to the waiting room.");

        if (emptySlots.get_value() == 0) {
            Output.log("The waiting room is full.");
            Output.log("P" + patient.getPatientId()
                    + " is waiting outside for an available seat.");
        }
        emptySlots.acquire();
        mutex.acquire();
        try {
            ((LinkedList<Patient>) patients).addFirst(patient);

            Output.log("Doctor " + doctorId + " returned Patient "
                    + patient.getPatientId()
                    + " back to the waiting room due to timeout.");
        } finally {
            mutex.release();
        }
        availablePatients.release();
    }

    public void close(int numberOfDoctors) throws InterruptedException {
        mutex.acquire();

        try {
            this.closed = true;
        } finally {
            mutex.release();
        }

        for (int i = 0; i < numberOfDoctors; i++) {
            availablePatients.release();
        }
    }

}
