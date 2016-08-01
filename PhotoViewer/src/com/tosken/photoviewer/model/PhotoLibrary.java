package com.tosken.photoviewer.model;

import rx.Observable;

/**
 * Created by Sebastian Greif on 01.08.2016.
 * Copyright di support 2016
 */
public interface PhotoLibrary {
    void scan();

    Observable<Photo> photos();
}
