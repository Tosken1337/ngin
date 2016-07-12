package com.tosken.ngin.geometry.loader;

import com.tosken.ngin.geometry.Geometry;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by Sebastian Greif on 29.06.2016.
 * Copyright di support 2016
 */
public interface GeometryLoader {
    Geometry load(Path file) throws IOException;
}
