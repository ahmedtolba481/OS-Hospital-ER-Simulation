import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class StatsBar extends HBox {
    private final int totalPatients;
    private final int capacity;
    private int treated, seats, rooms, sentBack, timeouts;
    private long startedAt;
    private long pausedAt;
    private long pausedTotal;
    private boolean clockRunning;
    private final Label treatedV, seatsV, roomsV, sentV, timeoutsV, clockV;
    private final Timeline clock;

    public StatsBar(int capacity, int doctors, int patients) {
        this.capacity = capacity;
        this.totalPatients = patients;
        getStyleClass().add("stats-bar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(14);
        treatedV = cell("TREATED", "0 / " + patients);
        seatsV = cell("SEATS", "0 / " + capacity);
        roomsV = cell("ROOMS", "0 / 2");
        sentV = cell("SENT BACK", "0");
        timeoutsV = cell("TIMEOUTS", "0");
        clockV = cell("ELAPSED", "00:00");
        startedAt = System.currentTimeMillis();
        clockRunning = true;
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private Label cell(String label, String value) {
        Label v = new Label(value);
        v.getStyleClass().add("stat-value");
        Label l = new Label(label);
        l.getStyleClass().add("stat-label");
        VBox box = new VBox(2, v, l);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("stat-cell");
        getChildren().add(box);
        return v;
    }

    private long effectiveMillis(long now) {
        long pausedSpan = pausedTotal + (pausedAt != 0 ? now - pausedAt : 0);
        return now - startedAt - pausedSpan;
    }

    private void tick() {
        if (!clockRunning) return;
        long s = effectiveMillis(System.currentTimeMillis()) / 1000;
        clockV.setText(String.format("%02d:%02d", s / 60, s % 60));
    }

    public void pauseClock() {
        if (clockRunning && pausedAt == 0) {
            pausedAt = System.currentTimeMillis();
        }
    }

    public void resumeClock() {
        if (pausedAt != 0) {
            pausedTotal += System.currentTimeMillis() - pausedAt;
            pausedAt = 0;
        }
    }

    public long elapsedSeconds() {
        return effectiveMillis(System.currentTimeMillis()) / 1000;
    }

    public void stopClock() {
        clockRunning = false;
        clock.stop();
        tick();
    }

    public int treatedCount() { return treated; }

    public void handle(SimEvent e) {
        switch (e.type) {
            case SEATED: seats++; seatsV.setText(seats + " / " + capacity); break;
            case TAKEN: seats--; seatsV.setText(seats + " / " + capacity); break;
            case RETURNED: seats++; sentBack++; seatsV.setText(seats + " / " + capacity); sentV.setText("" + sentBack); break;
            case ROOM_ACQUIRED: rooms++; roomsV.setText(rooms + " / 2"); break;
            case ROOM_RELEASED: rooms--; roomsV.setText(rooms + " / 2"); break;
            case ROOM_TIMEOUT: timeouts++; timeoutsV.setText("" + timeouts); break;
            case LEFT_HOSPITAL: treated++; treatedV.setText(treated + " / " + totalPatients); break;
            default: break;
        }
    }
}
