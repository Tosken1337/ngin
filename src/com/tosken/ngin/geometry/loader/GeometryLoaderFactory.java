package com.tosken.ngin.geometry.loader;

import java.util.EnumMap;

/**
 * Created by Sebastian Greif on 29.06.2016.
 * Copyright di support 2016
 */
public class GeometryLoaderFactory {
    public enum Format {
        OBJ(".obj");

        Format(final String suffix) {
            this.suffix = suffix;
        }

        private String suffix;
    }

    private static EnumMap<Format, GeometryLoader> loaderRegistry = new EnumMap<>(Format.class);

    static {
        loaderRegistry.put(Format.OBJ, new ObjLoader());
    }

    private GeometryLoaderFactory() {}

    public static GeometryLoader create(final String filename) {
        return null;
    }

    public static GeometryLoader create(final Format format) {
        return loaderRegistry.get(format);
    }
}
