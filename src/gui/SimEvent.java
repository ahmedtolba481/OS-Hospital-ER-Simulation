import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimEvent {
    public enum Type {
        ARRIVED, WAITING_SEAT, SEATED, TAKEN, ASSIGNED, TRYING_ROOM,
        ROOM_ACQUIRED, TREATING, FINISHED_TREATING, ROOM_RELEASED,
        DOCTOR_AVAILABLE, ROOM_TIMEOUT, SENDING_BACK, ROOM_FULL,
        WAITING_OUTSIDE, RETURNED, LEFT_HOSPITAL
    }

    public final Type type;
    public final int patientId;
    public final int doctorId;
    public final String time;
    public final String text;

    private SimEvent(Type type, int patientId, int doctorId, String time, String text) {
        this.type = type;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.time = time;
        this.text = text;
    }

    private static final Pattern LINE = Pattern.compile("^\\[(\\d\\d:\\d\\d:\\d\\d)\\] (.*)$");

    private static final Object[][] RULES = {
        {Type.ARRIVED,           Pattern.compile("^Patient (\\d+) arrived at the hospital\\.$"), "p"},
        {Type.WAITING_SEAT,      Pattern.compile("^Patient (\\d+) is waiting for an available seat\\.$"), "p"},
        {Type.SEATED,            Pattern.compile("^Patient (\\d+) entered the waiting room\\.$"), "p"},
        {Type.TAKEN,             Pattern.compile("^A Doctor took Patient (\\d+) from waiting room\\.$"), "p"},
        {Type.ASSIGNED,          Pattern.compile("^Doctor (\\d+) is assigned to P(\\d+)$"), "dp"},
        {Type.TRYING_ROOM,       Pattern.compile("^Doctor (\\d+) is trying to acquire a treatment room\\.$"), "d"},
        {Type.ROOM_ACQUIRED,     Pattern.compile("^Doctor (\\d+) acquired a treatment room\\.$"), "d"},
        {Type.TREATING,          Pattern.compile("^Doctor (\\d+) begins treating P(\\d+)$"), "dp"},
        {Type.FINISHED_TREATING, Pattern.compile("^Doctor (\\d+) finished treating P(\\d+)$"), "dp"},
        {Type.ROOM_RELEASED,      Pattern.compile("^Doctor (\\d+) released the treatment room\\.$"), "d"},
        {Type.DOCTOR_AVAILABLE,  Pattern.compile("^Doctor (\\d+) is now available\\.$"), "d"},
        {Type.ROOM_TIMEOUT,      Pattern.compile("^Doctor (\\d+) could not acquire a treatment room\\.$"), "d"},
        {Type.SENDING_BACK,      Pattern.compile("^Doctor (\\d+) is sending P(\\d+) back to the waiting room\\.$"), "dp"},
        {Type.ROOM_FULL,         Pattern.compile("^The waiting room is full\\.$"), ""},
        {Type.WAITING_OUTSIDE,   Pattern.compile("^P(\\d+) is waiting outside for an available seat\\.$"), "p"},
        {Type.RETURNED,          Pattern.compile("^Doctor (\\d+) returned Patient (\\d+) back to the waiting room due to timeout\\.$"), "dp"},
        {Type.LEFT_HOSPITAL,     Pattern.compile("^Patient (\\d+) left the hospital after treatment\\.$"), "p"},
    };

    public static SimEvent parse(String line) {
        if (line == null) return null;
        if (line.contains("was interrupted")) return null;
        Matcher lm = LINE.matcher(line);
        if (!lm.matches()) return null;
        String time = lm.group(1);
        String msg = lm.group(2);
        for (Object[] rule : RULES) {
            Matcher m = ((Pattern) rule[1]).matcher(msg);
            if (!m.matches()) continue;
            String groups = (String) rule[2];
            int d = -1, p = -1;
            if (groups.equals("p")) p = Integer.parseInt(m.group(1));
            else if (groups.equals("d")) d = Integer.parseInt(m.group(1));
            else if (groups.equals("dp")) { d = Integer.parseInt(m.group(1)); p = Integer.parseInt(m.group(2)); }
            return new SimEvent((Type) rule[0], p, d, time, msg);
        }
        return null;
    }
}
