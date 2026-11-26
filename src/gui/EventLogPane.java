import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class EventLogPane extends VBox {
    private final VBox entries = new VBox(4);
    private final ScrollPane scroll = new ScrollPane(entries);
    private int count;

    public EventLogPane() {
        getStyleClass().add("log-pane");
        setPrefWidth(320);
        Label title = new Label("EVENT LOG");
        title.getStyleClass().add("log-title");
        entries.setPadding(new Insets(8));
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("log-scroll");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        getChildren().addAll(title, scroll);
        setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);
    }

    public void add(SimEvent e) {
        String color;
        switch (e.type) {
            case ARRIVED: color = Theme.TEAL; break;
            case SEATED: color = Theme.TEAL; break;
            case ASSIGNED: case TREATING: color = Theme.CYAN; break;
            case ROOM_TIMEOUT: case ROOM_FULL: case WAITING_OUTSIDE: color = Theme.RED; break;
            case SENDING_BACK: case RETURNED: color = Theme.PURPLE; break;
            case FINISHED_TREATING: case LEFT_HOSPITAL: case DOCTOR_AVAILABLE: color = Theme.GREEN; break;
            default: color = Theme.MUTED;
        }
        Circle dot = new Circle(4, Color.web(color));
        Label time = new Label(e.time);
        time.getStyleClass().add("log-time");
        Label msg = new Label(e.text);
        msg.getStyleClass().add("log-msg");
        msg.setWrapText(true);
        HBox row = new HBox(8, dot, time, msg);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("log-entry");
        entries.getChildren().add(row);
        if (++count > 250) entries.getChildren().remove(0);
        Platform.runLater(() -> scroll.setVvalue(1.0));
    }
}
