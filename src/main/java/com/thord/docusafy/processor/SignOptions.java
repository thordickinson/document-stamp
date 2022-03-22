package com.thord.docusafy.processor;

public class SignOptions {
    private final String color;
    private final VerticalPosition verticalPosition;
    private final float transparency;
    private final float rotation;

    public SignOptions(String color, VerticalPosition verticalPosition, float transparency, float rotation) {
        this.color = color;
        this.verticalPosition = verticalPosition;
        this.transparency = transparency;
        this.rotation = rotation;
    }

    public SignOptions() {
        this("#CCCCCC", VerticalPosition.BOTTOM, 0.25f, 45f);
    }

    public String getColor() {
        return color;
    }

    public float getRotation() {
        return rotation;
    }

    public VerticalPosition getVerticalPosition() {
        return verticalPosition;
    }

    public float getTransparency() {
        return transparency;
    }

}