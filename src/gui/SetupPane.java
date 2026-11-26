import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SetupPane extends VBox {

    public interface StartHandler {
        void onStart(int capacity, int doctors, int patients);
    }

    private final TextField capacityField;
    private final TextField doctorsField;
    private final TextField patientsField;
    private final Label error = new Label();

    public SetupPane(StartHandler handler) {
        getStyleClass().add("setup-root");
        setAlignment(Pos.CENTER);
        setSpacing(0);

        VBox card = new VBox(18);
        card.getStyleClass().add("setup-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(460);

        Label title = new Label("CITY GENERAL ER");
        title.getStyleClass().add("setup-title");
        Label sub = new Label("Night Shift - Producer-Consumer simulation with semaphores");
        sub.getStyleClass().add("setup-sub");
        sub.setWrapText(true);

        capacityField = field("4");
        doctorsField = field("3");
        patientsField = field("10");

        card.getChildren().addAll(
            title, sub,
            row("Waiting room seats", capacityField, 1, 12),
            row("Doctors on shift", doctorsField, 1, 6),
            row("Patients arriving", patientsField, 1, 30),
            chips(),
            error,
            startButton(handler)
        );
        error.getStyleClass().add("setup-error");
        getChildren().add(card);
    }

    private HBox chips() {
        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        for (String s : new String[]{"2 treatment rooms", "3s room timeout", "2-4s treatment"}) {
            Label c = new Label(s);
            c.getStyleClass().add("chip");
            chips.getChildren().add(c);
        }
        return chips;
    }

    private TextField field(String value) {
        TextField f = new TextField(value);
        f.getStyleClass().add("num-field");
        f.setPrefWidth(64);
        return f;
    }

    private HBox row(String label, TextField f, int min, int max) {
        Label l = new Label(label);
        l.getStyleClass().add("setup-label");
        Button minus = stepper("-", f, -1, min, max);
        Button plus = stepper("+", f, 1, min, max);
        minus.getStyleClass().add("stepper");
        plus.getStyleClass().add("stepper");
        HBox box = new HBox(10, l, new javafx.scene.layout.Region(), minus, f, plus);
        box.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.layout.HBox.setHgrow(box.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    private Button stepper(String text, TextField f, int delta, int min, int max) {
        Button b = new Button(text);
        b.setOnAction(e -> {
            int v = parse(f, min) + delta;
            f.setText("" + Math.max(min, Math.min(max, v)));
        });
        return b;
    }

    private int parse(TextField f, int fallback) {
        try { return Integer.parseInt(f.getText().trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    private Button startButton(StartHandler handler) {
        Button start = new Button("START NIGHT SHIFT");
        start.getStyleClass().add("btn-primary");
        start.setMaxWidth(Double.MAX_VALUE);
        start.setOnAction(e -> {
            int cap = parse(capacityField, 0);
            int docs = parse(doctorsField, 0);
            int pats = parse(patientsField, -1);
            if (cap < 1 || cap > 12 || docs < 1 || docs > 6 || pats < 1 || pats > 30) {
                error.setText("Seats 1-12, doctors 1-6, patients 1-30.");
                return;
            }
            handler.onStart(cap, docs, pats);
        });
        return start;
    }
}
