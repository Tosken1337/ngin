package com.tosken.photoviewer.app;

import com.google.inject.Inject;
import com.tosken.ngin.application.Application;
import com.tosken.ngin.application.DesktopApplication;
import com.tosken.photoviewer.io.image.PhotoLibraryResourceManager;
import com.tosken.photoviewer.model.PhotoLibrary;
import com.tosken.photoviewer.rendering.PhotoRenderer;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Sebastian Greif on 29.07.2016.
 * Copyright di support 2016
 */
public class PhotoViewerApplication extends DesktopApplication {
    private static final Logger log = LoggerFactory.getLogger(PhotoViewerApplication.class);

    @Inject
    private PhotoLibrary library;

    @Inject
    private PhotoRenderer renderer;

    @Override
    protected void onUpdateFrame(final double elapsedMillis) {

    }

    @Override
    protected void onRenderFrame(final double elapsedMillis) {
        renderer.render(elapsedMillis);
    }

    @Override
    protected void onInitApplication() {
        library.scan();
        renderer.setLibrary(library);
    }

    @Override
    protected void onInitGL() {
        renderer.initGl();
    }

    @Override
    protected void onCloseApplication() {

    }

    @Override
    protected void onKeyEvent(final int action, final int key) {
        if (action == GLFW_RELEASE && key == GLFW_KEY_SPACE) {
            //renderer.getCamera()
        }
    }

    @Override
    protected void onFrameBufferSizeChanged(final Vector2i frameBufferSize) {
        log.info("Framebuffer size changed to {} x {}", frameBufferSize.x, frameBufferSize.y);
        if (frameBufferSize.x > 0) {
            renderer.onFrameBufferSizeChanged(frameBufferSize);
        }
    }
}
