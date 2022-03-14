package com.thord.docusafy.processor;

public class SignOptions {
    private final String color;
    private final int verticalPosition;

    public SignOptions(String color, int verticalPosition){
        this.color = color;
        this.verticalPosition = verticalPosition;
    }

    public String getColor() {
        return color;
    }

    public int getVerticalPosition() {
        return verticalPosition;
    }

}