package com.tosken.photoviewer.io;

import com.tosken.photoviewer.model.Photo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Created by Sebastian Greif on 29.07.2016.
 * Copyright di support 2016
 */
public final class PhotoFileScanner {
    private static final Logger log = LoggerFactory.getLogger(PhotoFileScanner.class);

    private static final Pattern IMAGE_PATTERN = Pattern.compile("[^\\.*](.*\\.[jJ][pP][gG]|.*\\.[bB][mM][pP]|.*\\.[pP][nN][gG]|.*\\.[jJ][pP][eE][gG])");
    private static final PathMatcher IMAGE_MATCHER = FileSystems.getDefault().getPathMatcher("regex:" + IMAGE_PATTERN.toString());


    private PhotoFileScanner() {
    }

    public static Observable<Photo> scan(final Path root, final int scanDepth, final Function<Path, Photo> photoCreator) {
        return Observable.create(subscriber -> {
            try {
                Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), scanDepth, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (IMAGE_MATCHER.matches(file)) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(photoCreator.apply(file));
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (Files.isHidden(dir)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        } else {
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                });
            } catch (IOException e) {
                log.error("Unable to traverse file tree while scanning for media int path {}", root, e);
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
    }
}
