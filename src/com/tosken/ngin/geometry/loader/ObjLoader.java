package com.tosken.ngin.geometry.loader;

import com.tosken.ngin.geometry.Geometry;
import com.tosken.ngin.geometry.IndexedGeometry;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Sebastian Greif on 29.06.2016.
 * Copyright di support 2016
 */
public class ObjLoader implements GeometryLoader {

    private static final Pattern POSITION = Pattern.compile("^v\\s{1,4}([+-]?([0-9]*[.])?[0-9]+)\\s([+-]?([0-9]*[.])?[0-9]+)\\s([+-]?([0-9]*[.])?[0-9]+)");
    private static final Pattern TEXCOORD = Pattern.compile("^vt\\s{1,4}([+-]?([0-9]*[.])?[0-9]+)\\s([+-]?([0-9]*[.])?[0-9]+)");
    private static final Pattern TRIANGLE = Pattern.compile("^f\\s{1,4}([0-9]+(/[0-9]*){0,2})\\s([0-9]+(/[0-9]*){0,2})\\s([0-9]+(/[0-9]*){0,2})");

    @Override
    public Geometry load(final Path file) throws IOException {

        final ArrayList<Vector3f> positions = new ArrayList<>();
        final ArrayList<Vector2f> texCoords = new ArrayList<>();
        final ArrayList<MultiIndex> idx = new ArrayList<>();

        //final ArrayList
        Files.lines(file)
                .forEachOrdered(line -> {
                    final Predicate<String> isVertex = POSITION.asPredicate();
                    final Predicate<String> isTexCoord = TEXCOORD.asPredicate();
                    final Predicate<String> isTriangle = TRIANGLE.asPredicate();
                    if (isVertex.test(line)) {
                        line = line.replaceFirst("^v\\s{1,4}", "");
                        final String[] coords = line.split("\\s");
                        final float x = Float.parseFloat(coords[0]);
                        final float y = Float.parseFloat(coords[1]);
                        final float z = Float.parseFloat(coords[2]);
                        positions.add(new Vector3f(x, y, z));
                    } else if (isTexCoord.test(line)) {
                        line = line.replaceFirst("^v\\s{1,4}", "");
                        final String[] coords = line.split("\\s");
                        final float u = Float.parseFloat(coords[0]);
                        final float v = Float.parseFloat(coords[1]);
                        texCoords.add(new Vector2f(u, v));
                    } else if (isTriangle.test(line)) {
                        line = line.replaceFirst("^f\\s{1,4}", "");
                        final String[] vertexIndices = line.split("\\s");

                        for (String indexString : vertexIndices) {
                            final MultiIndex index = new MultiIndex();

                            final String[] indices = indexString.split("/");
                            index.positionIdx = Integer.parseInt(indices[0]);
                            if (indices.length > 1) {
                                index.texIdx = Integer.parseInt(indices[1]);
                            }


                            idx.add(index);
                        }
                    }
                });

        final List<Integer> indexData = idx.stream()
                .map(multiIndex -> multiIndex.positionIdx)
                .collect(Collectors.toList());

        if (!texCoords.isEmpty()) {
            final ArrayList<Vector2f> resolvedTexCoords = new ArrayList<>(positions.size());
            //@TODO not yet ready

            return null;
        } else {
            return new IndexedGeometry(positions, indexData);
        }


    }

    private class MultiIndex {
        int positionIdx = -1;
        int texIdx = -1;
        int normalIdx = -1;
    }

}
