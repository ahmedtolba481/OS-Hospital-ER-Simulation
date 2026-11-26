import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

// Runs the original simulation classes unchanged. It tees System.out so the
// GUI sees every Output.log line, and wires the threads exactly like
// Hospital.main does. No original file is modified.
public class SimDriver {

    public interface Listener {
        void onEvent(SimEvent event);
        void onFinished();
    }

    private final int capacity;
    private final int numberOfDoctors;
    private final int numberOfPatients;
    private final Listener listener;
    private final List<Thread> threads = new ArrayList<>();
    private volatile boolean running;
    private PrintStream originalOut;

    public SimDriver(int capacity, int numberOfDoctors, int numberOfPatients, Listener listener) {
        this.capacity = capacity;
        this.numberOfDoctors = numberOfDoctors;
        this.numberOfPatients = numberOfPatients;
        this.listener = listener;
    }

    public void start() {
        running = true;
        PaceGate.setPaused(false);
        originalOut = System.out;
        System.setOut(new PrintStream(originalOut) {
            @Override
            public void println(String s) {
                // Pause gate: while paused, the calling simulation thread
                // parks here, so the whole run freezes at the log lines.
                PaceGate.awaitIfPaused();
                super.println(s);
                if (!running) return;
                SimEvent e = SimEvent.parse(s);
                if (e != null) listener.onEvent(e);
            }
        });

        WaitingRoom waitingRoom = new WaitingRoom(capacity);
        TreatmentRoomManager treatmentRoomManager = new TreatmentRoomManager(2);
        Doctor[] doctors = new Doctor[numberOfDoctors];
        for (int i = 0; i < numberOfDoctors; i++) {
            doctors[i] = new Doctor(i + 1, waitingRoom, treatmentRoomManager);
        }
        Thread[] patients = new Thread[numberOfPatients];
        for (int i = 0; i < numberOfPatients; i++) {
            patients[i] = new Thread(new Patient(i + 1, waitingRoom));
        }
        for (Doctor d : doctors) threads.add(d);
        for (Thread p : patients) threads.add(p);

        Thread supervisor = new Thread(() -> {
            try {
                for (Doctor d : doctors) d.start();
                for (Thread p : patients) p.start();
                for (Thread p : patients) p.join();
                waitingRoom.close(numberOfDoctors);
                for (Doctor d : doctors) d.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.setOut(originalOut);
                running = false;
                if (!Thread.currentThread().isInterrupted()) listener.onFinished();
            }
        });
        threads.add(supervisor);
        supervisor.start();
    }

    public void stop() {
        running = false;
        PaceGate.setPaused(false);
        for (Thread t : threads) t.interrupt();
        if (originalOut != null) System.setOut(originalOut);
    }
}
