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
import rx.Observable;
import rx.functions.Func2;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * ngin
 * User: Sebastian
 * Date: 30.07.2016
 * Time: 18:16
 */
public class PhotoLibraryResourceManager {
    private final PhotoLibrary library;

    public PhotoLibraryResourceManager(final PhotoLibrary library) {
        this.library = library;
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
                            //final Dimension thumbSize = ImageUtils.readImageSize(new ByteArrayInputStream(thumbnailData));
                            resource.exifThumb = Optional.ofNullable(thumbnailData);
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
        public Optional<byte[]> exifThumb;
    }

    public static class PhotoTextureResource {
        public Photo photo;
        public Optional<Texture> texture;
    }
}
