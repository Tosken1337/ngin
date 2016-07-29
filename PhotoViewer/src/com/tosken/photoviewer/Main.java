package com.tosken.photoviewer;

import com.tosken.ngin.application.DesktopApplication;
import com.tosken.photoviewer.app.PhotoViewerApplication;

/**
 * Created by Sebastian Greif on 29.07.2016.
 * Copyright di support 2016
 */
public class Main {
    public static void main(String[] args) {
        final DesktopApplication.Configuration appConfig = new DesktopApplication.Configuration();
        appConfig.windowMode = DesktopApplication.Configuration.WindowMode.FullscreenWindow;
        appConfig.numSamples = 4;
        appConfig.resizable = false;
        appConfig.windowTitle = "PhotoViewer";

        final PhotoViewerApplication app = new PhotoViewerApplication();
        app.run(appConfig);
    }
}
