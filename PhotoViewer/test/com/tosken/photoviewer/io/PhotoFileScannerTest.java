package com.tosken.photoviewer.io;

import com.tosken.photoviewer.model.Photo;
import org.junit.Test;
import rx.Observable;

import java.nio.file.Paths;

/**
 * ngin
 * User: Sebastian
 * Date: 30.07.2016
 * Time: 16:32
 */
public class PhotoFileScannerTest {
    @Test
    public void scan() throws Exception {
        final Observable<Photo> photoStream = PhotoFileScanner.scan(Paths.get("photos/"), 1, path -> new Photo(path, -1, -1));
        photoStream.subscribe(System.out::println);
    }
}