package sample.utils;

import javafx.geometry.Point2D;

/**
 * Created by vladstarikov on 15.05.16.
 */
public class PointPolar {

    private double radius;
    private double angle;

    public PointPolar(double angle, double radius) {
        this.angle = angle;
        this.radius = radius;
    }

    public PointPolar(Point2D point) {
        this.angle = toAngle(point.getX(), point.getY());
        this.radius = toRadius(point.getX(), point.getY());
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public Point2D toPoint2D() {
        return toPoint2D(angle, radius);
    }

    public static Point2D toPoint2D(PointPolar point) {
        return toPoint2D(point.angle, point.radius);
    }

    public static Point2D toPoint2D(double angle, double radius) {
        double radians = Math.toRadians(angle);
        return new Point2D(radius * Math.cos(radians), radius * Math.sin(radians));
    }

    public static double toAngle(double x, double y) {
        double rad;
        if (x < 0) {
            rad = Math.atan(y / x) + Math.PI;
        } else if (x > 0) {
            if (y >= 0) rad = Math.atan(y / x);
            else rad = Math.atan(y / x) + 2.0 * Math.PI;
        } else {
            if (y > 0) rad = Math.PI / 2.0;
            else if (y < 0) rad = 3 * Math.PI / 2.0;
            else rad = Double.NaN;
        }
        return Math.toDegrees(rad);
    }

    public static double toRadius(double x, double y) {
        return Math.sqrt(x*x + y*y);
    }

    public static double toX(double angle, double radius) {
        return radius * Math.cos(Math.toRadians(angle));
    }

    public static double toY(double angle, double radius) {
        return radius * Math.sin(Math.toRadians(angle));
    }

}
