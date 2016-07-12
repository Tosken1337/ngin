package com.tosken.ngin.geometry;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Created by Sebastian Greif on 29.06.2016.
 * Copyright di support 2016
 */
public class Geometry {
    private ArrayList<Vector3f> vertices;

    public Geometry(final ArrayList<Vector3f> vertices) {
        this.vertices = vertices;
    }

    public float[] vertexData() {

        final float[] dataBuffer = new float[vertices.size() * 3];
        for (int i = 0; i < vertices.size(); i++) {
            final Vector3f vertex = vertices.get(i);
            dataBuffer[i * 3] = vertex.x;
            dataBuffer[i * 3 + 1] = vertex.y;
            dataBuffer[i * 3 + 2] = vertex.z;
        }

        return dataBuffer;
    }
}
