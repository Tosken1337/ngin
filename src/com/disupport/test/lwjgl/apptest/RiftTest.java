package com.disupport.test.lwjgl.apptest;

import com.tosken.ngin.application.RiftApplication;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public class RiftTest {
    public static void main(String[] args) {
        final RiftApplication.Configuration config = new RiftApplication.Configuration();

        final SampleRiftApplication app = new SampleRiftApplication();
        app.run(config);
    }
}
