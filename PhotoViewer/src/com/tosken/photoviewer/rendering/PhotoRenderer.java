package com.tosken.photoviewer.rendering;

import com.tosken.photoviewer.model.PhotoLibrary;
import org.joml.Vector2i;

/**
 * Created by Sebastian Greif on 01.08.2016.
 * Copyright di support 2016
 */
public interface PhotoRenderer {

    void onFrameBufferSizeChanged(Vector2i frameBufferSize);

    void setLibrary(PhotoLibrary library);

    void initGl();

    void render(double elapsedMillis);
}
