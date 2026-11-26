import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

// Small vector people used on the floor: a patient figure (coloured gown,
// head, body) and a doctor figure (white coat, stethoscope, status cross).
// Purely visual, no simulation logic.
public final class Figures {

    private static final String SKIN = "#F2C9A0";
    public static final double W = 30, H = 36;

    // Patient: round head, rounded-shoulder body in the patient's colour.
    public static Group patient(String colorHex) {
        Group g = new Group();
        Rectangle body = new Rectangle(1, 15, 28, 21);
        body.setArcWidth(20); body.setArcHeight(20);
        body.setFill(Color.web(colorHex));
        body.setStroke(Color.web("#0B1220"));
        body.setStrokeWidth(1.5);
        Circle head = new Circle(15, 8, 7.5, Color.web(SKIN));
        head.setStroke(Color.web("#0B1220"));
        head.setStrokeWidth(1.5);
        g.getChildren().addAll(body, head);
        return g;
    }

    // Doctor: white coat, dark hair cap, stethoscope, coloured cross badge
    // that follows the doctor's status colour.
    public static class Doctor extends Group {
        private final Rectangle crossV = new Rectangle(), crossH = new Rectangle();

        public Doctor(double scale) {
            double s = scale;
            Rectangle coat = new Rectangle(1 * s, 15 * s, 28 * s, 21 * s);
            coat.setArcWidth(20 * s); coat.setArcHeight(20 * s);
            coat.setFill(Color.web("#F1F5F9"));
            coat.setStroke(Color.web("#94A3B8"));
            coat.setStrokeWidth(1.2);
            Circle head = new Circle(15 * s, 8 * s, 7.5 * s, Color.web(SKIN));
            head.setStroke(Color.web("#94A3B8"));
            head.setStrokeWidth(1.2);
            Rectangle cap = new Rectangle(7.5 * s, 0.5 * s, 15 * s, 6 * s);
            cap.setArcWidth(10 * s); cap.setArcHeight(10 * s);
            cap.setFill(Color.web("#2DD4BF"));
            Line steth1 = new Line(9 * s, 17 * s, 13 * s, 27 * s);
            Line steth2 = new Line(21 * s, 17 * s, 17 * s, 27 * s);
            for (Line l : new Line[]{steth1, steth2}) {
                l.setStroke(Color.web("#334155"));
                l.setStrokeWidth(1.6);
            }
            Circle chest = new Circle(15 * s, 29 * s, 2.2 * s, Color.web("#334155"));
            crossV.setX(21 * s); crossV.setY(22 * s);
            crossV.setWidth(2.4 * s); crossV.setHeight(8 * s);
            crossH.setX(18.2 * s); crossH.setY(24.8 * s);
            crossH.setWidth(8 * s); crossH.setHeight(2.4 * s);
            setStatusColor(Theme.GREEN);
            getChildren().addAll(coat, head, cap, steth1, steth2, chest, crossV, crossH);
        }

        public void setStatusColor(String hex) {
            crossV.setFill(Color.web(hex));
            crossH.setFill(Color.web(hex));
        }
    }

    private Figures() {}
}
