package com.tosken.photoviewer.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * ngin
 * User: Sebastian
 * Date: 30.07.2016
 * Time: 18:23
 */
public class ImageUtils {

    public static Dimension readImageSize(final Path file) throws IOException {
        return readImageSize(new FileInputStream(file.toFile()));
        /*int width = -1;
        int height = -1;

        try (ImageInputStream ims = ImageIO.createImageInputStream(file.toFile())) {
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

        return new Dimension(width, height);*/
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
