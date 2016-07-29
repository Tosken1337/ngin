package com.tosken.photoviewer.app;

import com.tosken.ngin.application.DesktopApplication;
import org.joml.Vector2i;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Sebastian Greif on 29.07.2016.
 * Copyright di support 2016
 */
public class PhotoViewerApplication extends DesktopApplication {
    @Override
    protected void onUpdateFrame(final double elapsedMillis) {

    }

    @Override
    protected void onRenderFrame(final double elapsedMillis) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void onInitApplication() {

    }

    @Override
    protected void onInitGL() {
        //	96, 125, 139
        glClearColor(96 / 255f, 125 / 255f, 139 / 255f, 1f);
    }

    @Override
    protected void onCloseApplication() {

    }

    @Override
    protected void onKeyEvent(final int action, final int key) {

    }

    @Override
    protected void onFrameBufferSizeChanged(final Vector2i frameBufferSize) {

    }
}
