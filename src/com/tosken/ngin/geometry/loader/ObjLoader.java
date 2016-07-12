package com.tosken.ngin.geometry.loader;

import com.tosken.ngin.geometry.Geometry;
import com.tosken.ngin.geometry.IndexedGeometry;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by Sebastian Greif on 29.06.2016.
 * Copyright di support 2016
 */
public class ObjLoader implements GeometryLoader {

    private static final Pattern VERTEX = Pattern.compile("^v\\s{1,4}([+-]?([0-9]*[.])?[0-9]+)\\s([+-]?([0-9]*[.])?[0-9]+)\\s([+-]?([0-9]*[.])?[0-9]+)");
    private static final Pattern TRIANGLE = Pattern.compile("^f\\s{1,4}([0-9]+(/[0-9]*){0,2})\\s([0-9]+(/[0-9]*){0,2})\\s([0-9]+(/[0-9]*){0,2})");

    @Override
    public Geometry load(final Path file) throws IOException {

        final ArrayList<Vector3f> vertices = new ArrayList<>();
        final ArrayList<Integer> idx = new ArrayList<>();

        //final ArrayList
        Files.lines(file)
                .forEachOrdered(line -> {
                    final Predicate<String> isVertex = VERTEX.asPredicate();
                    final Predicate<String> isTriangle = TRIANGLE.asPredicate();
                    if (isVertex.test(line)) {
                        line = line.replaceFirst("^v\\s{1,4}", "");
                        final String[] coords = line.split("\\s");
                        final float x = Float.parseFloat(coords[0]);
                        final float y = Float.parseFloat(coords[1]);
                        final float z = Float.parseFloat(coords[2]);
                        vertices.add(new Vector3f(x, y, z));
                    } else if (isTriangle.test(line)) {
                        line = line.replaceFirst("^f\\s{1,4}", "");
                        final String[] vertexIndices = line.split("\\s");

                        for (String indexString : vertexIndices) {
                            final String[] indices = indexString.split("/");
                            final int vertexIndex = Integer.parseInt(indices[0]);
                            idx.add(vertexIndex);
                        }
                    }
                });

        return new IndexedGeometry(vertices, idx);
    }
}
