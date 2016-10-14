package sh.calaba.instrumentationbackend.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Message;
import android.os.MessageQueue;
import sh.calaba.instrumentationbackend.Logger;

import java.lang.reflect.Field;
import java.util.List;

/**
 * This class solves the issue of Android waiting for the message queue of the application
 * to become idle before continuing from {@link android.app.Instrumentation#startActivitySync}.
 * If the message queue is never idle (like the application continuously pushing stuff to
 * the message queue), startActivitySync will not return. Usually, the idle message queue
 * would invoke the idle handler added in {android.app.Instrumentation#prePerformCreate}.
 * That is an ActivityGoing linked with its waiting activity.
 * We therefore do the following:
 *    - Wait for the instrumentations waiting activities to have set the activity.
 *    - When it is no longer null, we will fire off the idle handler.
 *    - It is possible that the message queue will be idle at the exact same time,
 *      but that should not be a problem as we do not concurrently modify things.
 */
public class ActivityLaunchedWaiter implements Runnable {
    private final MessageQueue instrumentationMessageQueue;
    private final Class activityGoingClass;

    public ActivityLaunchedWaiter(Instrumentation instrumentation) {
        super();

        try {
            activityGoingClass = Class.forName("android.app.Instrumentation$ActivityGoing");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        instrumentationMessageQueue = getMessageQueue(instrumentation);

    }

    @Override
    public void run() {
        while (true) {
            List<MessageQueue.IdleHandler> idleHandlers = getIdleHandlers(instrumentationMessageQueue);

            for (int i = 0; i < idleHandlers.size(); i++) {
                MessageQueue.IdleHandler idleHandler;

                try {
                    idleHandler = idleHandlers.get(i);
                } catch (IndexOutOfBoundsException ex) {
                    continue;
                }

                if (idleHandler.getClass().isAssignableFrom(activityGoingClass)) {
                    // We have found the right idleHandler.
                    Object activityWaiter = getActivityWaiterOfActivityGoing(idleHandler);

                    if (activityWaiter != null) {
                        Activity activity = getActivityOfActivityWaiter(activityWaiter);

                        if (activity != null) {
                            Logger.info("Activity is set in activity waiter. Invoking idle handlers");

                            boolean remove = idleHandler.queueIdle();

                            if (remove) {
                                synchronized (instrumentationMessageQueue) {
                                    instrumentationMessageQueue.removeIdleHandler(idleHandler);
                                }
                            }

                            return;
                        }
                    }
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                Logger.info("Asked to stop waiting for activity waiter.");
                break;
            }
        }
    }

    private static Object getActivityWaiterOfActivityGoing(MessageQueue.IdleHandler activityGoing) {
        try {
            Field activityWaiterField = activityGoing.getClass().getDeclaredField("mWaiter");
            activityWaiterField.setAccessible(true);

            return activityWaiterField.get(activityGoing);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Activity getActivityOfActivityWaiter(Object activityWaiter) {
        try {
            Field activityField = activityWaiter.getClass().getDeclaredField("activity");
            activityField.setAccessible(true);

            return (Activity) activityField.get(activityWaiter);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static MessageQueue getMessageQueue(Instrumentation instrumentation) {
        try {
            Field messageQueueField = Instrumentation.class.getDeclaredField("mMessageQueue");
            messageQueueField.setAccessible(true);

            return (MessageQueue) messageQueueField.get(instrumentation);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<MessageQueue.IdleHandler> getIdleHandlers(MessageQueue messageQueue) {
        try {
            Field messageQueueField = MessageQueue.class.getDeclaredField("mIdleHandlers");
            messageQueueField.setAccessible(true);

            return (List<MessageQueue.IdleHandler>) messageQueueField.get(messageQueue);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
