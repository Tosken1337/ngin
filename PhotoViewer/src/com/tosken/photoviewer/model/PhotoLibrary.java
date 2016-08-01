package com.tosken.photoviewer.model;

import com.tosken.photoviewer.io.PhotoFileScanner;
import rx.Observable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * ngin
 * User: Sebastian
 * Date: 30.07.2016
 * Time: 18:04
 */
public class PhotoLibrary {
    private final Path root;

    private List<Photo> photos;

    public PhotoLibrary(Path root) {
        this.root = root;
    }

    public void scan() {
        PhotoFileScanner.scan(root, 1, Photo::fromPath)
                .toSortedList((photo, photo2) -> photo.getFile().compareTo(photo2.getFile()))
                .subscribe(photoList -> photos = new ArrayList<>(photoList));
    }

    public Observable<Photo> photos() {
        return Observable.from(photos);
    }
}
