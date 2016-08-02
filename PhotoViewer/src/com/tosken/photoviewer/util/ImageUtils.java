package com.tosken.photoviewer.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifThumbnailDirectory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;

import static com.drew.metadata.exif.ExifDirectoryBase.TAG_ORIENTATION;

/**
 * ngin
 * User: Sebastian
 * Date: 30.07.2016
 * Time: 18:23
 */
public class ImageUtils {

    public static int readExifRotation(final Path path) {
        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
            if (metadata.containsDirectoryOfType(ExifIFD0Directory.class)) {
                final ExifIFD0Directory infoDir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                final int orientation = infoDir.getInt(TAG_ORIENTATION);
                switch (orientation) {
                    case 1:
                    case 2:
                        return 0;
                    case 6:
                    case 5:
                        return 270;
                    case 3:
                    case 4:
                        return 180;
                    case 7:
                    case 8:
                        return 90;
                }
            }
            return 0;
        } catch (ImageProcessingException | IOException | MetadataException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static byte[] readThumbnail(final Path path) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
            if (metadata.containsDirectoryOfType(ExifThumbnailDirectory.class)) {
                final ExifThumbnailDirectory thumbDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
                final byte[] thumbnailData = thumbDir.getThumbnailData();
                return thumbnailData;
            } else {
                return null;
            }
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Dimension readImageSize(final Path file) throws IOException {
        return readImageSize(new FileInputStream(file.toFile()));
    }

    public static Dimension readImageSize(final InputStream stream) throws IOException {
        int width = -1;
        int height = -1;

        try (ImageInputStream ims = ImageIO.createImageInputStream(stream)) {
            Iterator<ImageReader> im = ImageIO.getImageReaders(ims);
            if (im.hasNext()) {
                ImageReader ir = im.next();
                ir.setInput(ims);

                int widthTmp = ir.getWidth(0);
                int heightTmp = ir.getHeight(0);
                if (widthTmp != -1 && heightTmp != -1) {
                    width = widthTmp;
                    height = heightTmp;
                }
                ir.dispose();
            }
        }

        return new Dimension(width, height);
    }
}
