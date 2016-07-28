package com.tosken.ngin.application;

import com.tosken.ngin.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public abstract class Application {
    protected static final Logger log = LoggerFactory.getLogger(Application.class);

    protected Keyboard keyboard = new Keyboard();
}
