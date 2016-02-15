package sample.CompassView;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

/**
 * Created by vladstarikov on 10.02.16.
 */
public class CompassView extends Group {

    private double angleOffset = 90;

    private Group pointer;

    public CompassView() {
        //this.getChildren().add(createBackground());

        int radius = 120;
        pointer = createPointer(0, radius);
        this.getChildren().add(createOuterRim(radius));
        this.getChildren().add(createTicks(12, 2, radius, radius + 10, radius + 18));
        this.getChildren().add(pointer);
    }

    private Point2D polarToDecart(double angle, double distance) {
        double r = angle * Math.PI / 180;
        return new Point2D(distance * Math.cos(r), distance * Math.sin(r));
    }

    private Circle createOuterRim(double radius) {
        Circle circle = new Circle(radius, Color.TRANSPARENT);
        circle.setStroke(Color.BLACK);
        return circle;
    }

    private Group createTicks(int majorTicks, int minorTicks, double startRadius, double endRadius, double labelsRadius) {
        Group groupTicks = new Group();
        double majorTickStep = 360. / majorTicks;
        for (int i = 0; i < majorTicks; i++) {
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
            double minorTickStep = majorTickStep / (minorTicks + 1);
            for (int j = 1; j <= minorTicks; j++) {
                startPos = polarToDecart(majorAngle + j * minorTickStep, startRadius);
                endPos = polarToDecart(majorAngle + j * minorTickStep, endRadius - 2);
                groupTicks.getChildren().add(new Line(startPos.getX(), startPos.getY(), endPos.getX(), endPos.getY()));
            }
        }
        return groupTicks;
    }

    private Group createPointer(double angle, double radius) {
        Group pointer = new Group();
        pointer.resize(240, 240);
        System.out.println(angle);
        angle = 360 - angle + angleOffset - 10;
        System.out.println(angle);
        Arc arc = new Arc(0, 0, radius, radius, angle, 20);
        arc.setStroke(Color.BLUE);
        arc.setStrokeWidth(3);
        arc.setType(ArcType.OPEN);
        pointer.getChildren().add(arc);
        return pointer;
    }

    public void setAzimuth(double azimuth) {
        Arc arc = (Arc) pointer.getChildren().get(0);
        arc.setStartAngle(360 - azimuth + angleOffset - 10);
    }

}
