package sample.view;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import static sample.utils.PointPolar.toPoint2D;

/**
 * Created by vladstarikov on 10.02.16.
 */
public class CompassView extends Pane {

    private Point2D center = new Point2D(0, 0);
    private double size;
    private double angleOffset = 90;

    private Pointer azimuthPointer;
    private Group groupTicks = new Group();
    private Group points = new Group();

    public CompassView() {
        widthProperty().addListener((observable, oldValue, newValue) -> draw());
        heightProperty().addListener((observable, oldValue, newValue) -> draw());
        this.getChildren().add(azimuthPointer = new Pointer());
        this.getChildren().add(points);
        this.getChildren().add(groupTicks);
        draw();
    }

    private void draw() {
        center = new Point2D(getWidth() / 2.0, getHeight() / 2.0);
        size = Math.min(getWidth(), getHeight());
        double radius = size / 2.0;
        double outerRimRadius = radius - 20;

        drawOuterRim(outerRimRadius);
        drawTicks(12, 2, outerRimRadius, outerRimRadius + 10, outerRimRadius + 18);
        azimuthPointer.draw(center, outerRimRadius);
        points.resize(size, size);
        points.setLayoutX(center.getX());
        points.setLayoutY(center.getY());
    }

    private void drawOuterRim(double radius) {
        Circle circle = new Circle(center.getX(), center.getY(), radius, Color.TRANSPARENT);
        circle.setStroke(Color.BLACK);
        this.getChildren().add(circle);
    }

    private void drawTicks(int major, int minor, double startRadius, double endRadius, double labelsRadius) {
        groupTicks.setLayoutX(center.getX());
        groupTicks.setLayoutY(center.getY());
        groupTicks.getChildren().clear();
        double majorTickStep = 360. / major;
        for (int i = 0; i < major; i++) {
            //Lines
            double majorAngle = i * majorTickStep - angleOffset;
            Point2D startPos = toPoint2D(majorAngle, startRadius);
            Point2D endPos = toPoint2D(majorAngle, endRadius);
            groupTicks.getChildren().add(new Line(startPos.getX(), startPos.getY(), endPos.getX(), endPos.getY()));

            //Text
            Point2D textPos = toPoint2D(majorAngle, labelsRadius);
            Text text = new Text(String.valueOf((int) (majorAngle + angleOffset)));
            text.setX(-text.getLayoutBounds().getWidth()/2.);
            text.setY(text.getLayoutBounds().getHeight()/2.7);
            text.setLayoutX(textPos.getX());
            text.setLayoutY(textPos.getY());
            text.setRotate(majorAngle + 90);
            groupTicks.getChildren().add(text);

            //MinorTicks
            double minorTickStep = majorTickStep / (minor + 1);
            for (int j = 1; j <= minor; j++) {
                startPos = toPoint2D(majorAngle + j * minorTickStep, startRadius);
                endPos = toPoint2D(majorAngle + j * minorTickStep, endRadius - 2);
                groupTicks.getChildren().add(new Line(startPos.getX(), startPos.getY(), endPos.getX(), endPos.getY()));
            }
        }
    }

    public void setAzimuth(double azimuth) {
        azimuthPointer.setAngle(azimuth);
    }

    public void drawPoint(int angle, int distance) {
        Point2D point = toPoint2D(angle + angleOffset, distance / 250.0 * size / 2.0);//TODO: make autosize
        Circle circle = new Circle(point.getX(), point.getY(), 2);
        points.getChildren().add(circle);
        if (points.getChildren().size() > 25) points.getChildren().remove(0);
    }

    private class Pointer extends Group {

        private Arc arc = new Arc();
        private double angle = 0;

        public Pointer() {
            getChildren().add(this.arc = new Arc());
            this.arc.setStroke(Color.BLUE);
            this.arc.setLength(20);
            this.arc.setType(ArcType.OPEN);
            this.arc.setStrokeWidth(3);
        }

        double getAngle() {
            return angle;
        }

        void setAngle(double angle) {
            this.angle = angle;
            System.out.println("raw angle = " + angle);
            Arc arc = (Arc) this.getChildren().get(0);
            arc.setStartAngle(360 - angle + angleOffset - 10);
        }

        private void draw(Point2D center, double radius) {
            this.resize(radius * 2, radius * 2);
            arc.setRadiusX(radius);
            arc.setRadiusY(radius);
            arc.setCenterX(center.getX());
            arc.setCenterY(center.getY());
        }

    }

}
