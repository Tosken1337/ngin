package com.tosken.ngin.util;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Lwjgl
 * User: Sebastian
 * Date: 27.06.2015
 * Time: 20:28
 */
public class ResourceUtil {
    private ResourceUtil() {}

    public static String getResourceFileAsString(String resource, Class c) throws Exception {
        File f;
        final URL resUrl = c.getResource(resource);
        if (resUrl != null) {
            f = new File(c.getResource(resource).toURI());
        } else {
            f = Paths.get("resources", resource).toFile();
        }


        return Files.readAllLines(f.toPath()).stream()
                .collect(Collectors.joining("\n"));
    }

    public static File getResourceFile(String resource, Class c) throws Exception {
        return new File(c.getResource(resource).toURI());
    }
}
