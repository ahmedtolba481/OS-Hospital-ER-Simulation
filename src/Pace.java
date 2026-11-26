// Shared speed control for the simulation. The GUI changes the multiplier;
// at the default 1x every duration is exactly the original value, so the
// console program behaves exactly as before.
public class Pace {

    private static volatile double speed = 1.0;

    private Pace() {}

    public static double getSpeed() {
        return speed;
    }

    public static void setSpeed(double value) {
        if (value > 0) {
            speed = value;
        }
    }

    // Scale a duration in milliseconds for the current speed.
    public static long scale(long millis) {
        return Math.max(1, Math.round(millis / speed));
    }

    // Sleep like Thread.sleep, scaled by the current speed.
    public static void sleep(long millis) throws InterruptedException {
        Thread.sleep(scale(millis));
    }
}
