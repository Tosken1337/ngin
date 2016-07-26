package com.tosken.ngin.application;

import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public abstract class DesktopApplication extends Application {

    /**
     *
     */
    protected Configuration startConfig;

    /**
     *
     */
    protected Vector2i frameBufferSize = new Vector2i();

    private long window;

    protected DesktopApplication() {
    }

    public final void run(final Configuration configuration) {
        try {
            // Basic window and application callback setup
            initApplication(configuration);

            // Let the implementing class do it's initialization tasks
            onInitApplication();

            // Show window after all setup tasks
            glfwShowWindow(window);

            // Start gl loop
            glLoop();

            onCloseApplication();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void initApplication(final Configuration configuration) {

        GLFWErrorCallback errorCallback = new GLFWErrorCallback() {
            @Override
            public void invoke(final int error, final long description) {
                log.error("glfw error occured. Code: {} - Description: {}", error, description);
            }
        };
        glfwSetErrorCallback(errorCallback);

        org.lwjgl.system.Configuration.DEBUG.set(configuration.debug);

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // 1. Setup window related stuff
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, configuration.resizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, configuration.numSamples);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, 1);

        // Create the window
        window = glfwCreateWindow(configuration.windowSize.x, configuration.windowSize.y, configuration.windowTitle, configuration.fullscreen ? glfwGetPrimaryMonitor() : NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(window, GLFWFramebufferSizeCallback.create((window1, width, height) -> {
            frameBufferSize.set(width, height);
            GL.createCapabilities();
            onFrameBufferSizeChanged(frameBufferSize);
        }));

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            } else if (key != GLFW_KEY_ESCAPE){
                onKeyEvent(action, key);
            }
        });

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Get the resolution of the primary monitor
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        if (configuration.fullscreen) {
            frameBufferSize.set(videoMode.width(), videoMode.height());
            GL.createCapabilities();
            onFrameBufferSizeChanged(frameBufferSize);
        } else {
            // Center window
            glfwSetWindowPos(
                    window,
                    (videoMode.width() - configuration.windowSize.x) / 2,
                    (videoMode.height() - configuration.windowSize.y) / 2
            );
        }

        // Enable v-sync
        glfwSwapInterval(configuration.vSync ? 1 : 0);

        // Make the window visible
        //glfwShowWindow(window);
    }

    private void glLoop() {
        GL.createCapabilities();

        log.info("OpenGL version: {}", GL11.glGetString(GL11.GL_VERSION));
        log.info("Vendor: {}", GL11.glGetString(GL11.GL_VENDOR));

        onInitGL();

        glfwSetTime(0);
        double lastTime = 0;
        while (!glfwWindowShouldClose(window)) {
            final double currentTime = glfwGetTime();
            final double elapsedMillis = (currentTime - lastTime) * 1000;
            lastTime = currentTime;

            // Let the application perform application updates per frame (physics, input, ...)
            onUpdateFrame(elapsedMillis);

            // Let the application perform frame rendering
            onRenderFrame(elapsedMillis);

            // Swap the back buffer
            glfwSwapBuffers(window);

            // Poll for input events which will be handled in the next update
            glfwPollEvents();
        }
    }

    /**
     *
     * @param elapsedMillis
     */
    protected abstract void onUpdateFrame(final double elapsedMillis);

    /**
     *
     * @param elapsedMillis
     */
    protected abstract void onRenderFrame(final double elapsedMillis);

    /**
     *
     */
    protected abstract void onInitApplication();

    /**
     *
     */
    protected abstract void onInitGL();

    /**
     *
     */
    protected abstract void onCloseApplication();

    /**
     *
     * @param action
     * @param key
     */
    protected abstract void onKeyEvent(final int action, final int key);

    /**
     *
     * @param frameBufferSize
     */
    protected abstract void onFrameBufferSizeChanged(final Vector2i frameBufferSize);

    public static class Configuration {
        public boolean windowed = true;
        public boolean resizable = true;
        public Vector2i windowSize = new Vector2i(1024, 768);
        public int numSamples = 0;
        public boolean vSync = true;
        public boolean debug = false;
        public String windowTitle = "tosken.ngin application";
        public boolean fullscreen = false;
    }

}
