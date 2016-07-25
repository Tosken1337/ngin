package com.disupport.test.lwjgl.apptest;

import com.tosken.ngin.application.RiftApplication;
import org.joml.Matrix4f;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public class SampleRiftApplication extends RiftApplication {

    @Override
    protected void onUpdateFrame(final double elapsedMillis) {

    }

    @Override
    protected void onRenderFrame(double elapsedMillis, Matrix4f eyeViewM, Matrix4f projM) {

    }

    @Override
    protected void onInitApplication() {
        log.info("onInitApplication");
    }

    @Override
    protected void onInitGL() {
        log.info("onInitGL");

    }
}
