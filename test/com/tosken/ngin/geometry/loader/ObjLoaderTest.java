package com.tosken.ngin.geometry.loader;

import com.tosken.ngin.geometry.Geometry;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by Sebastian Greif on 29.06.2016.
 * Copyright di support 2016
 */
public class ObjLoaderTest {
    @Test
    public void load() throws Exception {
        final Geometry geometry = GeometryLoaderFactory.create(GeometryLoaderFactory.Format.OBJ).load(Paths.get("sibenik.obj"));
    }
}