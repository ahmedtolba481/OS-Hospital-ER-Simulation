// GUI-side pause gate. Every simulation log line passes through SimDriver's
// output tee, which waits on this gate, so while paused each simulation
// thread parks at its next Output.log call and the whole run freezes.
// The original classes are not modified.
public class PaceGate {

    private static final Object LOCK = new Object();
    private static volatile boolean paused;

    private PaceGate() {}

    public static boolean isPaused() {
        return paused;
    }

    public static void setPaused(boolean value) {
        synchronized (LOCK) {
            paused = value;
            if (!value) {
                LOCK.notifyAll();
            }
        }
    }

    public static void awaitIfPaused() {
        if (!paused) {
            return;
        }
        synchronized (LOCK) {
            while (paused) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
