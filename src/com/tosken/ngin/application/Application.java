package com.tosken.ngin.application;

import com.tosken.ngin.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.schedulers.ScheduledAction;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public abstract class Application {
    protected static final Logger log = LoggerFactory.getLogger(Application.class);

    private static Application instance;

    protected Keyboard keyboard = new Keyboard();

    private List<GlAction> glActions = new ArrayList<>();

    private Lock glActionLock = new ReentrantLock();

    private GlScheduler glScheduler = new GlScheduler();

    protected Application() {
        instance = this;
    }

    public interface GlAction {
        void execute();
    }

    public final void addGlAction(final GlAction action) {
        glActionLock.lock();
        try {
            glActions.add(action);
        } finally {
            glActionLock.unlock();
        }
    }

    public final void removeGlAction(final GlAction action) {
        glActionLock.lock();
        try {
            glActions.remove(action);
        } finally {
            glActionLock.unlock();
        }
    }

    protected final void executeGlActions() {
        glActionLock.lock();
        try {
            glActions.forEach(GlAction::execute);
            glActions.clear();
        } finally {
            glActionLock.unlock();
        }
    }

    public GlScheduler getGlScheduler() {
        return glScheduler;
    }

    public static Application getApplication() {
        return instance;
    }

    protected abstract void onInitApplication();

    protected abstract void onInitGL();

    protected abstract void onUpdateFrame(double elapsedMillis);

    protected abstract void onCloseApplication();

    protected abstract void onKeyEvent(final int action, final int key);


    /**
     * Scheduler implementation to perform actions on the gl thread
     */
    public class GlScheduler extends Scheduler {

        @Override
        public Worker createWorker() {
            return new GlWorker();
        }

        public final class GlWorker extends Scheduler.Worker {
            final CompositeSubscription tracking = new CompositeSubscription();
            @Override
            public Subscription schedule(final Action0 action0) {
                if (isUnsubscribed()) {
                    return Subscriptions.unsubscribed();
                }

                ScheduledAction action = new ScheduledAction(action0);
                tracking.add(action);
                action.add(Subscriptions.create(() -> tracking.remove(action)));

                GlAction glEvent = () -> {
                    if (!action.isUnsubscribed()) {
                        action.run();
                    }
                };

                addGlAction(glEvent);

                action.add(Subscriptions.create(() -> removeGlAction(glEvent)));

                return action;
            }

            @Override
            public Subscription schedule(final Action0 action0, final long l, final TimeUnit timeUnit) {
                return null;
            }

            @Override
            public void unsubscribe() {
                tracking.unsubscribe();
            }

            @Override
            public boolean isUnsubscribed() {
                return tracking.isUnsubscribed();
            }
        }

    }
}
