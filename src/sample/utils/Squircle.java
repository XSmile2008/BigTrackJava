package sample.utils;

import static java.lang.Math.sqrt;

/**
 * Created by vladstarikov on 17.05.16.
 */
public class Squircle {

    //(u,v) are circular coordinates in the domain {(u,v) | u² + v² ≤ 1}
    //(x,y) are square coordinates in the range [-1,1] x [-1,1]

    private static final double twosqrt2 = 2.0 * sqrt(2.0);

    // Elliptical Grid mapping
    // mapping a circular disc to a square region
    // input: (u,v) coordinates in the circle
    // output: (x,y) coordinates in the square
    public static double[] circleToSquare(double u, double v) {
        double u2 = u * u;
        double v2 = v * v;
        double twosqrt2u = u * twosqrt2;
        double twosqrt2v = v * twosqrt2;
        double subtermx = 2.0 + u2 - v2;
        double subtermy = 2.0 - u2 + v2;
        double x = 0.5 * sqrt(subtermx + twosqrt2u) - 0.5 * sqrt(subtermx - twosqrt2u);
        double y = 0.5 * sqrt(subtermy + twosqrt2v) - 0.5 * sqrt(subtermy - twosqrt2v);
        return new double[]{x, y};
    }

    // Elliptical Grid mapping
    // mapping a square region to a circular disc
    // input: (x,y) coordinates in the square
    // output: (u,v) coordinates in the circle
    public static double[] squareToCircle(double x, double y) {
        double u = x * sqrt(1.0 - y*y/2.0);
        double v = y * sqrt(1.0 - x*x/2.0);
        return new double[]{u, v};
    }
}
