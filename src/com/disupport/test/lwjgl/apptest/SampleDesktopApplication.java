package com.disupport.test.lwjgl.apptest;

import com.tosken.ngin.application.DesktopApplication;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public class SampleDesktopApplication extends DesktopApplication {

    @Override
    protected void onRenderFrame(final double elapsedMillis) {
        log.debug("onRenderFrame {}", elapsedMillis);
    }

    @Override
    protected void onInitApplication() {
        log.info("onInitApplication");
    }

    @Override
    protected void onInitGL() {
        log.info("onInitGL");
    }

    @Override
    protected void onFrameBufferSizeChanged(final Vector2i frameBufferSize) {
        GL11.glViewport(0, 0, frameBufferSize.x, frameBufferSize.y);
    }
}
