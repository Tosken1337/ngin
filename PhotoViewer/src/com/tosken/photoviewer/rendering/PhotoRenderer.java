package com.tosken.photoviewer.rendering;

import org.joml.Vector2i;

/**
 * Created by Sebastian Greif on 01.08.2016.
 * Copyright di support 2016
 */
public interface PhotoRenderer {

    void onFrameBufferSizeChanged(Vector2i frameBufferSize);

    void initGl();

    void render(double elapsedMillis);
}
