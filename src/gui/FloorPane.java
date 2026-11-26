import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

// The animated ER floor: outside queue, waiting room seats, doctor cards and
// the two treatment rooms. Purely visual; driven by parsed simulation events.
public class FloorPane extends StackPane {

    private static final double W = 940, H = 640;
    private static final double TOKEN = 36;

    private final Pane floor = new Pane();
    private final Pane tokenLayer = new Pane();
    private final VBox finishedOverlay;
    private final Label finishedStats = new Label();
    private final Label pausedBadge = new Label("PAUSED");

    private final Map<Integer, StackPane> tokens = new HashMap<>();
    private final Map<Integer, Deque<double[]>> moveQueues = new HashMap<>();
    private final Map<Integer, Boolean> animating = new HashMap<>();
    private final Map<Integer, StackPane> doctorCards = new HashMap<>();
    private final Map<Integer, Label> doctorStatus = new HashMap<>();
    private final Map<Integer, Figures.Doctor> doctorDots = new HashMap<>();
    private final Map<Integer, Integer> doctorRoom = new HashMap<>();
    private final Figures.Doctor[] roomDoctors = new Figures.Doctor[2];
    private final StackPane[] roomPods = new StackPane[2];
    private final Label[] roomLabels = new Label[2];
    private final ScaleTransition[] roomPulse = new ScaleTransition[2];

    private final List<Integer> waiting = new ArrayList<>();
    private final List<Integer> outside = new ArrayList<>();
    private final int capacity;
    private final int doctors;

    public FloorPane(int capacity, int doctors) {
        this.capacity = capacity;
        this.doctors = doctors;
        floor.setPrefSize(W, H);
        floor.setMaxSize(W, H);
        tokenLayer.setPrefSize(W, H);
        tokenLayer.setMaxSize(W, H);
        tokenLayer.setMouseTransparent(true);
        buildZones();
        StackPane board = new StackPane(floor, tokenLayer);
        board.setMaxSize(W, H);
        finishedOverlay = buildFinishedOverlay();
        pausedBadge.getStyleClass().add("paused-badge");
        pausedBadge.setVisible(false);
        pausedBadge.setMouseTransparent(true);
        StackPane.setAlignment(pausedBadge, Pos.TOP_CENTER);
        StackPane.setMargin(pausedBadge, new Insets(18, 0, 0, 0));
        getChildren().addAll(board, pausedBadge, finishedOverlay);
        getStyleClass().add("floor-wrap");
    }

    // Freeze or resume the purely visual loops while the run is paused.
    public void setPausedVisual(boolean value) {
        pausedBadge.setVisible(value);
        for (ScaleTransition st : roomPulse) {
            if (st != null) {
                if (value) {
                    st.pause();
                } else {
                    st.play();
                }
            }
        }
    }

    private void buildZones() {
        addZone(14, 76, 104, 470, "OUTSIDE");
        addDoor(44, 556, "ENTRANCE");
        addZone(132, 76, 340, 252, "WAITING ROOM");
        drawSeats();
        addZone(486, 76, 182, 470, "DOCTORS");
        drawDoctors();
        addZone(682, 76, 246, 300, "TREATMENT ROOMS");
        drawRooms();
        addZone(682, 470, 246, 76, "EXIT");
        Label arrow = new Label(">>");
        arrow.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        arrow.setStyle("-fx-text-fill: " + Theme.MUTED + ";");
        arrow.setLayoutX(766); arrow.setLayoutY(488);
        floor.getChildren().add(arrow);
    }

    // Cards shrink to fit the fixed DOCTORS zone (inner band y 116..536) for any doctor count.
    private double cardH() { return Math.min(66, (420.0 - (doctors - 1) * 10) / doctors); }
    private double cardY(int i) { return 116 + (i - 1) * (cardH() + 10); }

    private void addZone(double x, double y, double w, double h, String label) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(18); r.setArcHeight(18);
        r.setFill(Color.web(Theme.PANEL_DARK));
        r.setStroke(Color.web(Theme.BORDER));
        Label l = new Label(label);
        l.getStyleClass().add("zone-label");
        l.setLayoutX(x + 12); l.setLayoutY(y + 8);
        floor.getChildren().addAll(r, l);
    }

    private void addDoor(double x, double y, String label) {
        Rectangle d = new Rectangle(x, y, 52, 10);
        d.setArcWidth(6); d.setArcHeight(6);
        d.setFill(Color.web(Theme.TEAL));
        Label l = new Label(label);
        l.getStyleClass().add("zone-label");
        l.setLayoutX(x - 6); l.setLayoutY(y + 16);
        floor.getChildren().addAll(d, l);
    }

    private void drawSeats() {
        for (int i = 0; i < capacity && i < 12; i++) {
            double[] p = seatPos(i);
            Rectangle s = new Rectangle(p[0] - 3, p[1] - 3, TOKEN + 6, TOKEN + 6);
            s.setArcWidth(10); s.setArcHeight(10);
            s.setFill(Color.web(Theme.PANEL));
            s.setStroke(Color.web(Theme.BORDER));
            floor.getChildren().add(s);
        }
    }

    private void drawDoctors() {
        for (int i = 1; i <= doctors; i++) {
            double y = cardY(i);
            StackPane card = new StackPane();
            card.setLayoutX(498); card.setLayoutY(y);
            card.setPrefSize(156, cardH());
            card.getStyleClass().add("doctor-card");
            double fs = Math.min(1.0, (cardH() - 14) / Figures.H);
            Figures.Doctor dot = new Figures.Doctor(fs);
            Label name = new Label("DR " + i);
            name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            name.setStyle("-fx-text-fill: " + Theme.TEXT + ";");
            Label status = new Label("Available");
            status.getStyleClass().add("doctor-status");
            VBox texts = new VBox(3, name, status);
            texts.setAlignment(Pos.CENTER_LEFT);
            javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(10, dot, texts);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new javafx.geometry.Insets(0, 0, 0, 12));
            card.getChildren().add(box);
            doctorCards.put(i, card);
            doctorStatus.put(i, status);
            doctorDots.put(i, dot);
            floor.getChildren().add(card);
        }
    }

    private void drawRooms() {
        for (int i = 0; i < 2; i++) {
            double y = 116 + i * 136;
            StackPane pod = new StackPane();
            pod.setLayoutX(694); pod.setLayoutY(y);
            pod.setPrefSize(222, 120);
            pod.getStyleClass().add("room-pod");
            Label l = new Label("ROOM " + (i + 1) + "\nFree");
            l.getStyleClass().add("room-label");
            pod.getChildren().add(l);
            roomPods[i] = pod;
            roomLabels[i] = l;
            floor.getChildren().add(pod);
            Figures.Doctor rd = new Figures.Doctor(1.0);
            rd.setLayoutX(694 + 14); rd.setLayoutY(y + 60);
            rd.setVisible(false);
            roomDoctors[i] = rd;
            floor.getChildren().add(rd);
        }
    }

    private VBox buildFinishedOverlay() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("finished-overlay");
        Label title = new Label("ALL PATIENTS TREATED");
        title.getStyleClass().add("finished-title");
        Label sub = new Label("Simulation complete");
        sub.getStyleClass().add("finished-sub");
        finishedStats.getStyleClass().add("finished-stats");
        Button again = new Button("New Simulation");
        again.getStyleClass().add("btn-primary");
        again.setOnAction(e -> { if (onReset != null) onReset.run(); });
        box.getChildren().addAll(title, sub, finishedStats, again);
        box.setVisible(false);
        box.setMouseTransparent(false);
        return box;
    }

    private Runnable onReset;
    public void setOnReset(Runnable r) { this.onReset = r; }

    private double[] seatPos(int i) {
        return new double[]{132 + 26 + (i % 4) * 78, 116 + (i / 4) * 70};
    }

    private double[] outsidePos(int i) {
        return new double[]{14 + 12 + (i % 2) * 48, 120 + (i / 2) * 46};
    }

    private double[] doctorTransitPos(int d) {
        return new double[]{440, cardY(d) + cardH() * 0.25};
    }

    private double[] roomPos(int room) {
        return new double[]{694 + 93, 116 + room * 136 + 72};
    }

    public void handle(SimEvent e) {
        switch (e.type) {
            case ARRIVED: {
                StackPane t = makeToken(e.patientId);
                tokens.put(e.patientId, t);
                tokenLayer.getChildren().add(t);
                t.setLayoutX(48); t.setLayoutY(586);
                if (!outside.contains(e.patientId)) outside.add(e.patientId);
                layoutOutside();
                break;
            }
            case SEATED: {
                outside.remove((Integer) e.patientId);
                if (!waiting.contains(e.patientId)) waiting.add(e.patientId);
                layoutOutside(); layoutWaiting();
                break;
            }
            case TAKEN: {
                waiting.remove((Integer) e.patientId);
                layoutWaiting();
                break;
            }
            case ASSIGNED: {
                setDoctor(e.doctorId, "With P" + e.patientId, Theme.AMBER);
                moveToken(e.patientId, doctorTransitPos(e.doctorId), false);
                break;
            }
            case ROOM_ACQUIRED: {
                int room = freeRoom();
                if (room >= 0) {
                    doctorRoom.put(e.doctorId, room);
                    roomLabels[room].setText("ROOM " + (room + 1) + "\nReserved - Dr " + e.doctorId);
                    roomPods[room].getStyleClass().add("room-busy");
                    roomDoctors[room].setStatusColor(Theme.AMBER);
                    roomDoctors[room].setVisible(true);
                }
                break;
            }
            case TREATING: {
                Integer room = doctorRoom.get(e.doctorId);
                if (room != null) {
                    roomLabels[room].setText("ROOM " + (room + 1) + "\nDr " + e.doctorId + " treating P" + e.patientId);
                    startPulse(room);
                    roomDoctors[room].setStatusColor(Theme.CYAN);
                    moveToken(e.patientId, roomPos(room), false);
                }
                setDoctor(e.doctorId, "Treating P" + e.patientId, Theme.CYAN);
                break;
            }
            case FINISHED_TREATING: setDoctor(e.doctorId, "Finishing P" + e.patientId, Theme.TEAL); break;
            case ROOM_RELEASED: {
                Integer room = doctorRoom.remove(e.doctorId);
                if (room != null) {
                    stopPulse(room);
                    roomLabels[room].setText("ROOM " + (room + 1) + "\nFree");
                    roomPods[room].getStyleClass().remove("room-busy");
                    roomDoctors[room].setVisible(false);
                }
                break;
            }
            case DOCTOR_AVAILABLE: setDoctor(e.doctorId, "Available", Theme.GREEN); break;
            case ROOM_TIMEOUT: setDoctor(e.doctorId, "Room timeout", Theme.RED); break;
            case SENDING_BACK: setDoctor(e.doctorId, "Returning P" + e.patientId, Theme.PURPLE); break;
            case RETURNED: {
                waiting.add(0, e.patientId);
                layoutWaiting();
                break;
            }
            case LEFT_HOSPITAL: {
                moveToken(e.patientId, new double[]{762, 506}, true);
                break;
            }
            default: break;
        }
    }

    private int freeRoom() {
        for (int i = 0; i < 2; i++) if (!doctorRoom.containsValue(i)) return i;
        return -1;
    }

    private void setDoctor(int d, String status, String color) {
        Label s = doctorStatus.get(d);
        Figures.Doctor dot = doctorDots.get(d);
        if (s != null) s.setText(status);
        if (dot != null) dot.setStatusColor(color);
    }

    private StackPane makeToken(int patientId) {
        String col = Theme.patientColor(patientId);
        javafx.scene.Group fig = Figures.patient(col);
        fig.setEffect(new DropShadow(8, Color.web(col)));
        Label l = new Label("P" + patientId);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        l.setStyle("-fx-text-fill: #0B1220;");
        StackPane.setAlignment(l, Pos.BOTTOM_CENTER);
        l.setTranslateY(-3);
        StackPane t = new StackPane(fig, l);
        t.setPrefSize(TOKEN, TOKEN);
        t.setMaxSize(TOKEN, TOKEN);
        return t;
    }

    private void layoutWaiting() {
        for (int i = 0; i < waiting.size(); i++) moveToken(waiting.get(i), seatPos(i), false);
    }

    private void layoutOutside() {
        for (int i = 0; i < outside.size(); i++) moveToken(outside.get(i), outsidePos(i), false);
    }

    private void moveToken(int patientId, double[] pos, boolean fadeAfter) {
        StackPane t = tokens.get(patientId);
        if (t == null) return;
        Deque<double[]> q = moveQueues.computeIfAbsent(patientId, k -> new ArrayDeque<>());
        q.add(new double[]{pos[0], pos[1], fadeAfter ? 1 : 0});
        pump(patientId, t);
    }

    private void pump(int patientId, StackPane t) {
        if (Boolean.TRUE.equals(animating.get(patientId))) return;
        Deque<double[]> q = moveQueues.get(patientId);
        if (q == null || q.isEmpty()) return;
        double[] target = q.poll();
        while (q.size() > 1) {
            // Event burst: skip stale waypoints so the token catches up.
            target = q.poll();
        }
        if (target[2] == 0 && !q.isEmpty()) {
            t.setLayoutX(target[0]);
            t.setLayoutY(target[1]);
            pump(patientId, t);
            return;
        }
        final double[] dest = target;
        animating.put(patientId, true);
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(380),
            new KeyValue(t.layoutXProperty(), dest[0], javafx.animation.Interpolator.EASE_BOTH),
            new KeyValue(t.layoutYProperty(), dest[1], Interpolator.EASE_BOTH)));
        tl.setOnFinished(ev -> {
            animating.put(patientId, false);
            if (dest[2] == 1) {
                FadeTransition ft = new FadeTransition(Duration.millis(420), t);
                ft.setToValue(0);
                ft.setOnFinished(e2 -> { tokenLayer.getChildren().remove(t); pump(patientId, t); });
                ft.play();
            } else {
                pump(patientId, t);
            }
        });
        tl.play();
    }

    private void startPulse(int room) {
        stopPulse(room);
        ScaleTransition st = new ScaleTransition(Duration.millis(700), roomPods[room]);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.03); st.setToY(1.03);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.play();
        roomPulse[room] = st;
    }

    private void stopPulse(int room) {
        if (roomPulse[room] != null) {
            roomPulse[room].stop();
            roomPods[room].setScaleX(1.0);
            roomPods[room].setScaleY(1.0);
            roomPulse[room] = null;
        }
    }

    public void showFinished(int treated, int total, long seconds) {
        finishedStats.setText("Treated " + treated + " / " + total + " patients in " + seconds + "s");
        finishedOverlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(500), finishedOverlay);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();
    }
}
