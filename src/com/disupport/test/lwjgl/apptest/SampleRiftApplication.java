package com.disupport.test.lwjgl.apptest;

import com.tosken.ngin.application.RiftApplication;
import com.tosken.ngin.gl.FrameBufferObject;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengles.GLES20.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengles.GLES20.GL_DEPTH_BUFFER_BIT;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public class SampleRiftApplication extends RiftApplication {

    @Override
    protected void onUpdateFrame(final double elapsedMillis) {

    }

    @Override
    protected void onRenderFrame(double elapsedMillis, Matrix4f eyeViewM, Matrix4f projM, FrameBufferObject currentFrameBuffer) {
        currentFrameBuffer.bind();
        if (!currentFrameBuffer.isComplete()) {
            throw new RuntimeException("asd");
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        currentFrameBuffer.unbind();
    }

    @Override
    protected void onCloseApplication() {

    }

    @Override
    protected void onKeyEvent(int action, int key) {

    }

    @Override
    protected void onInitApplication() {
        log.info("onInitApplication");
    }

    @Override
    protected void onInitGL() {
        log.info("onInitGL");
        GL11.glClearColor(0.8f, 0.6f, 0.5f, 1.0f);

    }
}
