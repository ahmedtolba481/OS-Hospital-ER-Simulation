import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Output {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public static synchronized void log(String message) {
        String time = LocalTime.now().format(formatter);
        System.out.println("[" + time + "] " + message);
    }

    public static synchronized void printHeader(
            int capacity, int doctors, int patients) {
        System.out.println("\n");
        System.out.println("Waiting Room Capacity : " + capacity);
        System.out.println("Number of Doctors : " + doctors);
        System.out.println("Number of Patients : " + patients);
        System.out.println("Treatment Rooms : 2");
    }

    public static synchronized void printFooter() {
        System.out.println("\n");
        System.out.println("                    Finished                      ");
        System.out.println("==================================================");
    }
}