package com.tosken.photoviewer.model;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tosken.photoviewer.io.PhotoFileScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * ngin
 * User: Sebastian
 * Date: 30.07.2016
 * Time: 18:04
 */
public class SimplePhotoLibrary implements PhotoLibrary {
    private static final Logger log = LoggerFactory.getLogger(SimplePhotoLibrary.class);

    private Path root;

    private List<Photo> photos;

    @Inject
    public SimplePhotoLibrary(Path root) {
        this.root = root;
    }

    @Override
    public void scan() {
        PhotoFileScanner.scan(root, 1, Photo::fromPath)
                .toSortedList((photo, photo2) -> photo.getFile().compareTo(photo2.getFile()))
                .subscribe(photoList -> photos = new ArrayList<>(photoList));
        log.info("Scanned photo library from path {} contains {} photos", root, photos.size());
    }

    @Override
    public Observable<Photo> photos() {
        return Observable.from(photos);
    }

    @Override
    public List<Photo> photoList() {
        return photos;
    }
}
