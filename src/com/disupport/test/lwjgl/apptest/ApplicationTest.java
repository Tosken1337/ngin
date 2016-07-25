package com.disupport.test.lwjgl.apptest;

import com.tosken.ngin.application.DesktopApplication;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public class ApplicationTest {
    public static void main(String[] args) {
        final DesktopApplication.Configuration config = new DesktopApplication.Configuration();

        final SampleDesktopApplication app = new SampleDesktopApplication();
        app.run(config);

    }
}
