package sample.view;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by vladstarikov on 10.02.16.
 */
public class CompassView extends Group {

    private double size;
    private double angleOffset = 90;

    private Pointer azimuthPointer;

    private Group points = new Group();

    public CompassView() {
        size = 240;
        this.getChildren().add(azimuthPointer = new Pointer(size/2));
        this.getChildren().add(points);
        draw();
    }

    private void draw() {
        double radius = size / 2;
        drawOuterRim(radius);
        drawTicks(12, 2, radius, radius + 10, radius + 18);
        points.resize(size, size);
    }

    private Point2D polarToDecart(double angle, double distance) {
        double r = angle * Math.PI / 180;
        return new Point2D(distance * Math.cos(r), distance * Math.sin(r));
    }

    private void drawOuterRim(double radius) {
        Circle circle = new Circle(radius, Color.TRANSPARENT);
        circle.setStroke(Color.BLACK);
        this.getChildren().add(circle);
    }

    private void drawTicks(int major, int minor, double startRadius, double endRadius, double labelsRadius) {
        Group groupTicks = new Group();
        double majorTickStep = 360. / major;
        for (int i = 0; i < major; i++) {
            //Lines
            double majorAngle = i * majorTickStep - angleOffset;
            Point2D startPos = polarToDecart(majorAngle, startRadius);
            Point2D endPos = polarToDecart(majorAngle, endRadius);
            groupTicks.getChildren().add(new Line(startPos.getX(), startPos.getY(), endPos.getX(), endPos.getY()));

            //Text
            Point2D textPos = polarToDecart(majorAngle, labelsRadius);
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
                startPos = polarToDecart(majorAngle + j * minorTickStep, startRadius);
                endPos = polarToDecart(majorAngle + j * minorTickStep, endRadius - 2);
                groupTicks.getChildren().add(new Line(startPos.getX(), startPos.getY(), endPos.getX(), endPos.getY()));
            }
        }
        this.getChildren().add(groupTicks);
    }

    public void setAzimuth(double azimuth) {
        azimuthPointer.setAngle(azimuth);
    }

    public void drawPoint(int angle, int distance) {
        Point2D point = polarToDecart(angle + angleOffset, distance);
        Circle circle = new Circle(point.getX(), point.getX(), 5);
        points.getChildren().add(circle);
    }

    private class Pointer extends Group {

        private double angle = 0;

        Pointer(double radius) {
            draw(radius);
        }

        double getAngle() {
            return angle;
        }

        void setAngle(double angle) {
            this.angle = angle;
            Arc arc = (Arc) this.getChildren().get(0);
            arc.setStartAngle(360 - angle + angleOffset - 10);
        }

        private void draw(double radius) {
            this.resize(radius * 2, radius * 2);
            System.out.println(angle);
            angle = 360 - angle + angleOffset - 10;
            System.out.println(angle);
            Arc arc = new Arc(0, 0, radius, radius, angle, 20);
            arc.setStroke(Color.BLUE);
            arc.setStrokeWidth(3);
            arc.setType(ArcType.OPEN);
            this.getChildren().add(arc);
        }

    }

}
