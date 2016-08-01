package com.tosken.photoviewer.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.tosken.photoviewer.model.PhotoLibrary;
import com.tosken.photoviewer.model.SimplePhotoLibrary;
import com.tosken.photoviewer.rendering.DebugRenderer;
import com.tosken.photoviewer.rendering.PhotoRenderer;

import java.nio.file.Paths;

/**
 * Created by Sebastian Greif on 01.08.2016.
 * Copyright di support 2016
 */
public class ProductionModule extends AbstractModule {
    @Override
    protected void configure() {
        /*bind(Path.class)
                .annotatedWith(Names.named("LIBRARY_PATH"))
                .toInstance(Paths.get("photos/"));*/
        //bind(PhotoLibrary.class).to(SimplePhotoLibrary.class);
        bind(PhotoRenderer.class).to(DebugRenderer.class);
    }

    @Provides
    PhotoLibrary provideLibrary() {
        final SimplePhotoLibrary lib = new SimplePhotoLibrary(Paths.get("photos/"));
        return lib;
    }
}
