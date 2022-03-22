package com.thord.docusafy.util;

import java.util.Optional;

public class RotableLayer {
    private final float originalWidth;
    private final float originalHeight;
    private Float rotation = 0f;
    private Optional<Float> width;
    private Optional<Float> height;

    public RotableLayer(float originalWidth, float originalHeight) {
        this.originalHeight = originalHeight;
        this.originalWidth = originalWidth;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        if (this.rotation == rotation)
            return;
        this.rotation = rotation;
        this.width = Optional.empty();
        this.height = Optional.empty();
    }

    public Float getHeight() {
        if (!height.isPresent()) {

        }
        return height.get();
    }

    public Float getWidth() {
        if (!width.isPresent()) {

        }
        return width.get();
    }

}
