package com.tosken.photoviewer.io.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Sebastian Greif on 02.08.2016.
 * Copyright di support 2016
 */
public class Storage {
    private static final Logger log = LoggerFactory.getLogger(Storage.class);

    private final Path path;

    public Storage(final Path path) {
        this.path = path;
    }

    public void init() throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public Path saveAsImage(final Path originalFile, final byte[] thumbnailData) throws IOException {
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(thumbnailData));
        final File destinationFile = createUniqueMediaTempFile(originalFile.toFile(), "thumb_", "jpg");
        final boolean result = ImageIO.write(image, "jpg", destinationFile);
        image.flush();

        if (result) {
            return destinationFile.toPath();
        } else {
            throw new IOException("Unable to write to temporary image");
        }
    }

    private File createUniqueMediaTempFile(final File file, final String prefix, final String fileExtension) throws IOException {
        final String fileName = com.google.common.io.Files.getNameWithoutExtension(file.getName());
        return File.createTempFile(prefix + fileName + "_", "." + fileExtension, path.toFile());
    }
}
