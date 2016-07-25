package com.tosken.ngin.geometry;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Greif on 29.06.2016.
 * Copyright di support 2016
 */
public class IndexedGeometry extends Geometry {
    private List<Integer> indices;

    public IndexedGeometry(final ArrayList<Vector3f> vertices, final ArrayList<Vector2f> texCoords, final ArrayList<Integer> indices) {
        super(vertices, texCoords);
        this.indices = indices;
    }

    public IndexedGeometry(final ArrayList<Vector3f> vertices, final List<Integer> indices) {
        super(vertices);
        this.indices = indices;
    }

    public int[] indexData() {
        final int[] indexData = new int[indices.size()];

        for (int i = 0; i < indices.size(); i++) {
            indexData[i] = indices.get(i) - 1;
        }

        return indexData;
    }

}
