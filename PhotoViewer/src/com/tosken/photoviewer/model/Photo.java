package com.tosken.photoviewer.model;

import java.nio.file.Path;

/**
 * Created by Sebastian Greif on 29.07.2016.
 * Copyright di support 2016
 */
public class Photo {
    private final Path file;
    private final int width;
    private final int height;

    public Photo(final Path file, final int width, final int height) {
        this.file = file;
        this.width = width;
        this.height = height;
    }
}
