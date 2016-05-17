package sample;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import sample.utils.PointPolar;
import sample.utils.Squircle;

/**
 * Created by vladstarikov on 13.05.16.
 */
public class Stick extends Pane {

    //Controls
    private Label labelDebug = new Label();
    private Circle circleConstraint = new Circle();
    private Circle circleStick = new Circle();

    //Options
    private IOnControl listener;
    private int scale = 255;
    private boolean mapping = true;

    //Data
    private Point2D center = new Point2D(0, 0);
    private double radius;
    private double offset;
    private double constraintRadius;
    private double angle;

    public Stick() {
        widthProperty().addListener((observable, oldValue, newValue) -> draw());
        heightProperty().addListener((observable, oldValue, newValue) -> draw());
        setBackground(new Background(new BackgroundFill(Color.AQUA, null, null)));

        //Constraint
        circleConstraint.setFill(null);
        circleConstraint.setStroke(Color.BLACK);
        circleConstraint.setStrokeWidth(2);
        getChildren().add(circleConstraint);

        //Stick
        circleStick.setFill(Color.BLACK);
        getChildren().add(circleStick);

        //Debug label
        labelDebug.setTextFill(Color.RED);
        getChildren().add(labelDebug);

        setOnMouseDragged(event -> {
            Point2D pos = constrain(new Point2D(event.getX() - center.getX(), event.getY() - center.getY()), constraintRadius - offset);
            setCursor(Cursor.CLOSED_HAND);
            circleStick.setCenterX(pos.getX() + center.getX());
            circleStick.setCenterY(pos.getY() + center.getY());

            Point2D scaled = scale(pos, constraintRadius - offset, constraintRadius - offset, 1, 1);//Scaled
            if (mapping) {
                double[] mapped = Squircle.circleToSquare(scaled.getX(), scaled.getY());
                scaled = new Point2D(mapped[0], mapped[1]);
            }

            if (listener != null) listener.onControl((short) scaled.getX(), (short) scaled.getY());

            labelDebug.setText(String.format(
                    "drag: width = %.2f, height = %.2f, radius = %.2f" +
                            "\ncenter: x = %.2f, y = %.2f" +
                            "\nlayout: x = %.2f, y = %.2f" +
                            "\npos: x = %.2f, y = %.2f, angle = %.2f" +
                            "\nscaled" + (mapping ? "&mapped" : "") + ": x = %.2f, y = %.2f",
                    getWidth(), getHeight(), radius,
                    center.getX(), center.getY(),
                    event.getX(), event.getY(),
                    pos.getX(), pos.getY(), angle,
                    scaled.getX(), scaled.getY()));
        });

        setOnMouseReleased(event -> {
            circleStick.setCenterX(center.getX());
            circleStick.setCenterY(center.getY());
            if (listener != null) listener.onControl((short) 0, (short) 0);
        });

        draw();
    }

    private void draw() {
        this.center = new Point2D(getWidth() / 2.0, getHeight() / 2.0);
        this.radius = Math.min(getWidth(), getHeight()) / 2.0;
        this.constraintRadius = radius * 0.8;
        double stickRadius = radius / 2.0;
        this.offset = stickRadius - (radius - constraintRadius);

        //Constraint
        circleConstraint.setRadius(constraintRadius);
        circleConstraint.setCenterX(center.getX());
        circleConstraint.setCenterY(center.getY());

        //Stick
        circleStick.setRadius(stickRadius);
        circleStick.setCenterX(center.getX());
        circleStick.setCenterY(center.getY());
    }

    private Point2D constrain(Point2D point, double radius) {
        PointPolar polar = new PointPolar(point);
        if (polar.getRadius() > radius) {
            polar.setRadius(radius);
        }
        this.angle = polar.getAngle();
        return polar.toPoint2D();
    }

    private Point2D scale(Point2D point, double maxXFrom, double maxYFrom, double maxXTo, double maxYTo) {
        return new Point2D(point.getX() / maxXFrom * maxXTo, point.getY() / maxYFrom * maxYTo);
    }

    public boolean isMapping() {
        return mapping;
    }

    public void setMapping(boolean mapping) {
        this.mapping = mapping;
    }

    public void setOnControlListener(IOnControl listener) {
        this.listener = listener;
    }

    public interface IOnControl {

        void onControl(short x, short y);

    }

}
