package com.tosken.photoviewer.io.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.tosken.ngin.gl.Texture;
import com.tosken.photoviewer.model.Photo;
import com.tosken.photoviewer.model.PhotoLibrary;
import com.tosken.photoviewer.model.SimplePhotoLibrary;
import com.tosken.photoviewer.util.ImageUtils;
import org.omg.CORBA.PolicyHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func2;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * ngin
 * User: Sebastian
 * Date: 30.07.2016
 * Time: 18:16
 */
public class PhotoLibraryResourceManager {
    private static final Logger log = LoggerFactory.getLogger(PhotoLibraryResourceManager.class);

    private final PhotoLibrary library;

    private final Storage tmpStorage;

    public PhotoLibraryResourceManager(final PhotoLibrary library) {
        this.library = library;
        tmpStorage = new Storage(Paths.get("tmp/"));
        try {
            tmpStorage.init();
        } catch (IOException e) {
            log.error("Unable to init storage", e);
        }
    }

    public Observable<PhotoExifResource> loadExifData() {
        return library.photos()
                .map(photo -> {
                    PhotoExifResource resource = new PhotoExifResource();
                    resource.photo = photo;

                    final Dimension size;
                    try {
                        size = ImageUtils.readImageSize(photo.getFile());
                        photo.setWidth(size.width);
                        photo.setHeight(size.height);

                        Metadata metadata = ImageMetadataReader.readMetadata(photo.getFile().toFile());
                        if (metadata.containsDirectoryOfType(ExifThumbnailDirectory.class)) {
                            final ExifThumbnailDirectory thumbDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
                            final byte[] thumbnailData = thumbDir.getThumbnailData();
                            final Path thumbFile = tmpStorage.saveAsImage(photo.getFile().toAbsolutePath(), thumbnailData);

                            //final Dimension thumbSize = ImageUtils.readImageSize(new ByteArrayInputStream(thumbnailData));
                            resource.exifThumb = Optional.ofNullable(thumbFile);
                        }
                    } catch (IOException | ImageProcessingException e) {
                        e.printStackTrace();
                    }
                    return resource;
                });
    }

    public Observable<PhotoTextureResource> loadTextures(final int textureSize) {
        return null;
    }


    public static class PhotoExifResource {
        public Photo photo;
        public Optional<Path> exifThumb;
    }

    public static class PhotoTextureResource {
        public Photo photo;
        public Optional<Path> thumb;
    }
}
