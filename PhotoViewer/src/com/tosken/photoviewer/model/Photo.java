package com.tosken.photoviewer.model;

import java.nio.file.Path;

/**
 * Created by Sebastian Greif on 29.07.2016.
 * Copyright di support 2016
 */
public class Photo {
    private final Path file;
    private int width;
    private int height;
    private int rotation;

    public Photo(final Path file, final int width, final int height) {
        this.file = file;
        this.width = width;
        this.height = height;
    }

    public static Photo fromPath(final Path path) {
        return new Photo(path, -1, -1);
    }

    public Path getFile() {
        return file;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Photo{");
        sb.append("file=").append(file);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append('}');
        return sb.toString();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setOrientation(final int rotation) {
        this.rotation = rotation;
    }
}
