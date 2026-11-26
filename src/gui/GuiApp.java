import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GuiApp extends Application {

    private Stage stage;
    private SimDriver driver;
    private StatsBar statsBar;
    private FloorPane floorPane;
    private EventLogPane logPane;
    private Button pauseButton;
    private javafx.scene.control.ToggleButton speedDefault;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("City General ER - Night Shift");
        showSetup();
        stage.setWidth(1300);
        stage.setHeight(860);
        stage.setMinWidth(1290);
        stage.setMinHeight(850);
        stage.show();
        String auto = System.getProperty("autostart");
        if (auto != null) {
            String[] p = auto.split(",");
            Platform.runLater(() -> startSimulation(
                Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2])));
        }
    }

    private void showSetup() {
        stopDriver();
        SetupPane setup = new SetupPane(this::startSimulation);
        Scene scene = new Scene(setup, 1300, 860);
        attachCss(scene);
        stage.setScene(scene);
    }

    private void startSimulation(int capacity, int doctors, int patients) {
        stopDriver();
        BorderPane root = new BorderPane();
        root.getStyleClass().add("sim-root");

        statsBar = new StatsBar(capacity, doctors, patients);
        floorPane = new FloorPane(capacity, doctors);
        floorPane.setOnReset(this::showSetup);
        logPane = new EventLogPane();

        if (System.getProperty("noheader") == null) root.setTop(buildHeader());
        HBox center = System.getProperty("nofloor") != null ? new HBox(14, new Region())
            : System.getProperty("nolog") != null ? new HBox(14, floorPane)
            : new HBox(14, floorPane, logPane);
        center.setPadding(new Insets(0, 16, 0, 16));
        HBox.setHgrow(center.getChildren().get(0), Priority.ALWAYS);
        root.setCenter(center);
        if (System.getProperty("nobottom") == null) root.setBottom(buildBottomBar());

        Scene scene = new Scene(root, 1300, 860);
        attachCss(scene);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.SPACE) togglePause();
        });
        stage.setScene(scene);

        driver = new SimDriver(capacity, doctors, patients, new SimDriver.Listener() {
            @Override
            public void onEvent(SimEvent e) {
                Platform.runLater(() -> {
                    floorPane.handle(e);
                    statsBar.handle(e);
                    logPane.add(e);
                });
            }
            @Override
            public void onFinished() {
                Platform.runLater(() -> {
                    statsBar.stopClock();
                    if (pauseButton != null) {
                        PaceGate.setPaused(false);
                        pauseButton.setText("Pause");
                        pauseButton.setDisable(true);
                    }
                    floorPane.showFinished(statsBar.treatedCount(), patients, statsBar.elapsedSeconds());
                });
            }
        });
        if (System.getProperty("nodriver") == null) driver.start();
    }

    private void stopDriver() {
        PaceGate.setPaused(false);
        Pace.setSpeed(1.0);
        if (speedDefault != null) speedDefault.setSelected(true);
        if (pauseButton != null) { pauseButton.setText("Pause"); pauseButton.setDisable(false); }
        if (driver != null) { driver.stop(); driver = null; }
        if (ekgTimeline != null) { ekgTimeline.stop(); ekgTimeline = null; }
    }

    private HBox buildSpeedSelector() {
        Label label = new Label("SPEED");
        label.getStyleClass().add("speed-label");
        javafx.scene.control.ToggleGroup group = new javafx.scene.control.ToggleGroup();
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        double[] speeds = {0.5, 1.0, 2.0, 4.0};
        String[] names = {"0.5x", "1x", "2x", "4x"};
        for (int i = 0; i < speeds.length; i++) {
            javafx.scene.control.ToggleButton b = new javafx.scene.control.ToggleButton(names[i]);
            b.setToggleGroup(group);
            b.getStyleClass().add("speed-btn");
            final double v = speeds[i];
            b.setOnAction(e -> setSpeed(v));
            if (v == 1.0) { b.setSelected(true); speedDefault = b; }
            box.getChildren().add(b);
        }
        HBox wrap = new HBox(8, label, box);
        wrap.setAlignment(Pos.CENTER_LEFT);
        return wrap;
    }

    private void setSpeed(double value) {
        Pace.setSpeed(value);
        if (statsBar != null) statsBar.noteSpeed(value);
    }

    private void togglePause() {
        if (driver == null || pauseButton == null || pauseButton.isDisabled()) return;
        boolean nowPaused = !PaceGate.isPaused();
        PaceGate.setPaused(nowPaused);
        pauseButton.setText(nowPaused ? "Resume" : "Pause");
        if (nowPaused) {
            statsBar.pauseClock();
        } else {
            statsBar.resumeClock();
        }
        floorPane.setPausedVisual(nowPaused);
        if (ekgTimeline != null) {
            if (nowPaused) {
                ekgTimeline.pause();
            } else {
                ekgTimeline.play();
            }
        }
    }

    private HBox buildHeader() {
        Label title = new Label("CITY GENERAL ER");
        title.getStyleClass().add("app-title");
        Label sub = new Label("Night Shift - Bounded Waiting Room - Custom Semaphores - Doctor & Patient Threads");
        sub.getStyleClass().add("app-sub");
        VBox titles = new VBox(2, title, sub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        javafx.scene.layout.Pane ekgPane = buildEkg();
        HBox header = new HBox(16, titles, spacer, ekgPane);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 18, 10, 18));
        header.getStyleClass().add("app-header");
        return header;
    }

    private javafx.scene.layout.Pane buildEkg() {
        javafx.scene.layout.Pane pane = new javafx.scene.layout.Pane();
        pane.setPrefSize(240, 52);
        pane.setMaxSize(240, 52);
        pane.setMinSize(240, 52);
        pane.setStyle("-fx-background-color: " + Theme.BG + "; -fx-background-radius: 8;");
        ekgLine = new javafx.scene.shape.Polyline();
        ekgLine.setStroke(Color.web(Theme.TEAL));
        ekgLine.setStrokeWidth(2);
        for (int i = 0; i < EKG_N; i++) {
            ekgY[i] = 26;
            ekgLine.getPoints().addAll(i * 2.0, 26.0);
        }
        pane.getChildren().add(ekgLine);
        ekgTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(40), e -> tickEkg()));
        ekgTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        if (System.getProperty("noekgtimer") == null) ekgTimeline.play();
        return pane;
    }

    private static final int EKG_N = 120;
    private final double[] ekgY = new double[EKG_N];
    private javafx.scene.shape.Polyline ekgLine;
    private javafx.animation.Timeline ekgTimeline;
    private int ekgPhase;

    private void tickEkg() {
        System.arraycopy(ekgY, 1, ekgY, 0, EKG_N - 1);
        ekgPhase = (ekgPhase + 1) % 40;
        double y = 26;
        if (ekgPhase == 6) y = 20; else if (ekgPhase == 8) y = 31;
        else if (ekgPhase == 10) y = 6; else if (ekgPhase == 12) y = 46;
        ekgY[EKG_N - 1] = y;
        javafx.collections.ObservableList<Double> pts = ekgLine.getPoints();
        for (int i = 0; i < EKG_N; i++) pts.set(i * 2 + 1, ekgY[i]);
    }

    private HBox buildBottomBar() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        pauseButton = new Button("Pause");
        pauseButton.getStyleClass().add("btn-ghost");
        pauseButton.setOnAction(e -> togglePause());
        HBox speedBox = buildSpeedSelector();
        Button reset = new Button("New Simulation");
        reset.getStyleClass().add("btn-ghost");
        reset.setOnAction(e -> showSetup());
        HBox bar = new HBox(12, statsBar, spacer, speedBox, pauseButton, reset);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 12, 16));
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }

    private void attachCss(Scene scene) {
        java.net.URL css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
