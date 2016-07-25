package com.tosken.ngin.application;

import com.tosken.ngin.oculus.OculusHmd;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetTime;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public abstract class RiftApplication extends Application {

    protected OculusHmd hmd;

    protected RiftApplication() {
    }

    public final void run(final Configuration configuration) {
        try {
            // Basic window and application callback setup
            initApplication(configuration);

            // Let the implementing class do it's initialization tasks
            onInitApplication();

            // Start gl loop
            glLoop();
        } finally {
        }
    }

    private void glLoop() {
        GL.createCapabilities();

        log.info("OpenGL version: {}", GL11.glGetString(GL11.GL_VERSION));
        log.info("Vendor: {}", GL11.glGetString(GL11.GL_VENDOR));

        onInitGL();

        glfwSetTime(0);
        double lastTime = 0;
        Matrix4f viewM = new Matrix4f();
        Matrix4f projM = new Matrix4f();

        while (true) {
            final double currentTime = glfwGetTime();
            final double elapsedMillis = (currentTime - lastTime) * 1000;
            lastTime = currentTime;

            // Let the application perform application updates per frame (physics, input, ...)
            onUpdateFrame(elapsedMillis);

            // Let the application perform frame rendering
            onRenderFrame(elapsedMillis, viewM, projM);

            // Poll for input events which will be handled in the next update
            glfwPollEvents();
        }
    }

    private void initApplication(Configuration configuration) {
        hmd = new OculusHmd();
        try {
            hmd.init();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize hmd", e);
        }
    }

    protected abstract void onInitApplication();

    protected abstract void onInitGL();

    protected abstract void onUpdateFrame(double elapsedMillis);

    protected abstract void onRenderFrame(double elapsedMillis, Matrix4f eyeViewM, Matrix4f projM);


    public static class Configuration {
    }
}
