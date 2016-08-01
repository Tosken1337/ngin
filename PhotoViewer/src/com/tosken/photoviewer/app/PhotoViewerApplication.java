package com.tosken.photoviewer.app;

import com.google.inject.Inject;
import com.tosken.ngin.application.DesktopApplication;
import com.tosken.photoviewer.io.image.PhotoLibraryResourceManager;
import com.tosken.photoviewer.model.PhotoLibrary;
import com.tosken.photoviewer.rendering.PhotoRenderer;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Sebastian Greif on 29.07.2016.
 * Copyright di support 2016
 */
public class PhotoViewerApplication extends DesktopApplication {
    private static final Logger log = LoggerFactory.getLogger(PhotoViewerApplication.class);

    @Inject
    private PhotoLibrary library;

    private PhotoLibraryResourceManager libraryResourceManager;

    private PhotoRenderer renderer;

    @Override
    protected void onUpdateFrame(final double elapsedMillis) {

    }

    @Override
    protected void onRenderFrame(final double elapsedMillis) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void onInitApplication() {
        library.scan();
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
        if (frameBufferSize.x > 0) {
            log.info("Framebuffer size changed to {} x {}", frameBufferSize.x, frameBufferSize.y);
            GL11.glViewport(0, 0, frameBufferSize.x, frameBufferSize.y);

            /*projMat = new Matrix4f().perspective(((float) Math.toRadians(50.0f)), (float)frameBufferSize.x / (float)frameBufferSize.y, 0.1f, 100f);
            viewMat = new Matrix4f().lookAt(0.0f, 0.0f, 2.0f,
                    0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f);*/
        }
    }
}
