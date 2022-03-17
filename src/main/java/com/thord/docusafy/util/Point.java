package com.thord.docusafy.util;

public class Point {

    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    private double rad(double degree) {
        return (degree * (float) Math.PI) / 180;
    }

    public Point rotate(double degree) {
        double radian = rad(degree);
        double cos = Math.cos(radian);
        double sin = Math.sin(radian);
        double x1 = x * cos - y * sin;
        double y1 = y * cos + x * sin;
        return new Point(x1, y1);
    }

    public Point addX(double delta) {
        return new Point(this.x + delta, y);
    }

    public Point addY(double delta) {
        return new Point(this.x, y + delta);
    }

    public Point add(double deltaX, double deltaY) {
        return new Point(x + deltaX, y + deltaY);
    }

    public static double rotatedAngle(double angle) {
        return 90 - angle;
    }
}
