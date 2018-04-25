package sh.calaba.instrumentationbackend.actions;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.util.Pair;
import android.view.MotionEvent;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.query.Query;
import sh.calaba.instrumentationbackend.query.QueryResult;

public class MultiTouchGesture {
    private Map<String, Map<Object,Object>>  evaluatedQueries = new HashMap<String, Map<Object,Object>>();
    private Map<String, Object> multiTouchGestureMap;
    private Instrumentation instrumentation;
    private List<Gesture> pressedGestures;
    private List<Gesture> gesturesToPerform;
    private boolean hasPressedFirstGesture;

    public MultiTouchGesture(Map<String, Object> multiTouchGesture) {
        this.multiTouchGestureMap = multiTouchGesture;
        instrumentation = InstrumentationBackend.instrumentation;
        gesturesToPerform = new ArrayList<Gesture>();
        hasPressedFirstGesture = false;
    }

    public Map<String, Map<Object, Object>> getEvaluatedQueries() {
        return evaluatedQueries;
    }

    public void parseGesture() {
        List<Map<String, Object>> sentGestures = (ArrayList<Map<String, Object>>) multiTouchGestureMap.get("gestures");
        List<String> queryStrings = new ArrayList<String>();

        // We query before generating the gestures. TODO: Implement new class with temporary information
        for (Map<String, Object> gestureMap : sentGestures) {
            String queryString = (String) gestureMap.get("query_string");

            if (queryString != null) {
                queryStrings.add(queryString);
            }

            ArrayList<Map<String, Object>> sentTouches = (ArrayList<Map<String, Object>>) gestureMap.get("touches");

            for (Map<String,Object> sentTouch : sentTouches) {
                if (sentTouch.containsKey("query_string")) {
                    queryString = (String) sentTouch.get("query_string");

                    if (queryString != null) {
                        queryStrings.add(queryString);
                    }
                }
            }
        }

        long timeout = (long) ((Double) multiTouchGestureMap.get("query_timeout") * 1000);
        Map<String, Map<String, Integer>> evaluatedQueries = evaluateQueries(queryStrings, timeout);
        for (Map<String, Object> gestureMap : sentGestures) {
            String queryString = (String) gestureMap.get("query_string");
            Map<String,Integer> rect = evaluatedQueries.get(queryString);

            ArrayList<Map<String, Object>> sentTouches = (ArrayList<Map<String, Object>>) gestureMap.get("touches");

            Gesture gesture = new Gesture();
            int length = sentTouches.size();

            for (int i = 0; i < length; i++) {
                Map<String, Object> touch = sentTouches.get(i);
                Map<String,Integer> specificRect = rect;

                if (touch.containsKey("query_string")) {
                    String specificQueryString = (String) touch.get("query_string");

                    if (specificQueryString != null) {
                        specificRect = evaluatedQueries.get(specificQueryString);
                    }
                }

                int resultX = 0, resultY = 0, resultWidth = 0, resultHeight = 0;

                if (specificRect != null) {
                    resultX = specificRect.get("x");
                    resultY = specificRect.get("y");
                    resultWidth = specificRect.get("width");
                    resultHeight = specificRect.get("height");
                }

                int offsetX = (Integer) touch.get("offset_x");
                int offsetY = (Integer) touch.get("offset_y");
                int x, y;

                if (specificRect == null) {
                    x = offsetX;
                    y = offsetY;
                } else {
                    x = ((((Integer) touch.get("x")) * resultWidth)/100 + offsetX + resultX);
                    y = ((((Integer) touch.get("y")) * resultHeight)/100 + offsetY + resultY);
                }

                long time = (long) ((Double) touch.get("time") * 1000);
                long wait = (long) ((Double) touch.get("wait") * 1000);
                boolean release = (Boolean) touch.get("release");

                if (i == length - 1) {
                    release = true;
                }

                if (!gesture.hasEvents()) {
                    gesture.addEvent(new Event(new Coordinate(x, y), wait));
                } else {
                    gesture.addEvent(new Event(new Coordinate(x, y), 0l));

                    if (wait != 0l) {
                        gesture.addEvent(new Event(new Coordinate(x, y), wait), false);
                    } else {
                        gesture.setFling(true);
                    }
                }

                gesture.addTimeOffset(time);

                if (release) {
                    gesture.addEvent(new Event(gesture.upEvent().getCoordinate(), 0l), true);
                    long endTime = gesture.upEvent().getTime();
                    gesturesToPerform.add(gesture);
                    gesture = new Gesture(endTime);
                }
            }
        }
    }

    public void perform() {
        parseGesture();

        // Sometimes the keyguard window or a service pops up very briefly.
        // We handle it by waiting a fixed time
        tryWaitForKeyguard(2);

        long time;
        long startTime = SystemClock.uptimeMillis();
        long endTime = findEndTime();
        pressedGestures = new ArrayList<Gesture>();

        while ((time = SystemClock.uptimeMillis() - startTime) <= endTime) {
            releaseGestures(time);

            if (hasPressedFirstGesture) {
                pressGestures(time);

                // Instead of reassigning the time variable we only move gestures when we no longer offset the time
                moveGestures(time);
            } else {
                // We catch the security exception thrown when the keyguard is visible briefly and retry if it's the first press
                // Offset the startTime by the needed extra time if needed
                long currentTime = SystemClock.uptimeMillis();
                pressGestures(time);
                long timeTaken =  SystemClock.uptimeMillis() - currentTime;

                // Offset the start time (this changing the future values of time). This is done to keep relative timings consistent
                if (hasPressedFirstGesture) { // We just pressed our first gesture
                    startTime += timeTaken;
                }
            }
        }

        releaseGestures(endTime);
    }

    private Map<String, Map<String, Integer>> evaluateQueries(List<String> queryStrings, long timeout) {
        List<String> distinctQueryStrings = new ArrayList<String>();

        for (String queryString : queryStrings) {
            if (!distinctQueryStrings.contains(queryString)) {
                distinctQueryStrings.add(queryString);
            }
        }

        Map<String, Map<String, Integer>> evaluatedQueriesRect = new HashMap<String, Map<String, Integer>>();

        long endTime  = SystemClock.uptimeMillis() + timeout;

        do {
            evaluatedQueries.clear();
            evaluatedQueriesRect.clear();

            for (String queryString : distinctQueryStrings) {
                QueryResult queryResult = new Query(queryString, java.util.Collections.emptyList()).executeQuery();
                // For now we calculate the views location and save it. In an implementation in the future, we should save the actual views and use their coordinates later on.
                List<?> results = queryResult.asMappedList();

                if (results.size() == 0) {
                    break;
                } else {
                    Map<Object,Object> firstItem = (Map<Object, Object>) results.get(0);
                    evaluatedQueries.put(queryString, firstItem);
                    Map<String,Integer> rect = (Map<String, Integer>) firstItem.get("rect");
                    evaluatedQueriesRect.put(queryString, rect);
                }
            }

            if (evaluatedQueriesRect.size() == distinctQueryStrings.size()) {
                // All the queries have been evaluated
                return evaluatedQueriesRect;
            }

            // Avoid affecting the UI Thread and device performance too much
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (SystemClock.uptimeMillis() <= endTime);

        throw new RuntimeException("Could not find views '" + distinctQueryStrings.toString() + "'");
    }

    private void sendPointerSync(MotionEvent motionEvent) {
        instrumentation.sendPointerSync(motionEvent);
    }

    private void pressGestures(long currentTime) {
        int i = 0;

        while (i < gesturesToPerform.size()) {
            Gesture gesture = gesturesToPerform.get(i);

            // Sometimes the keyguard window or a service pops up very briefly.
            // We handle it by retrying - but only if no gestures are currently down to avoid mistiming gestures.
            int retries = hasPressedFirstGesture ? 1 : 10;
            SecurityException ex = null;

            for (int j = 0; j < retries; j++) {
                try {
                    if (gesture.shouldPress(currentTime)) {
                        sendPointerSync(gesture.generateDownEvent(pressedGestures));
                        hasPressedFirstGesture = true;
                        pressedGestures.add(gesture);
                        gesturesToPerform.remove(i);
                    } else {
                        i++;
                    }

                    ex = null;
                    break;
                } catch (SecurityException e) {
                    ex = e;

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException interruptedException) {
                        throw new RuntimeException(interruptedException);
                    }
                }
            }

            if (ex != null) {
                throw new SecurityException(ex);
            }
        }
    }

    private void releaseGestures(long currentTime) {
        int i = 0;

        while (i < pressedGestures.size()) {
            Gesture gesture = pressedGestures.get(i);

            if (gesture.shouldRelease(currentTime)) {
                // Ensure that the gesture will always move to its end-coordinate
                long uptimeMillis = SystemClock.uptimeMillis();
                moveGestures(currentTime, uptimeMillis);

                sendPointerSync(gesture.generateUpEvent(pressedGestures, uptimeMillis+1));
                pressedGestures.remove(i);
            } else {
                i++;
            }
        }
    }

    private void moveGestures(long currentTime) {
        moveGestures(currentTime, null);
    }

    private void moveGestures(long currentTime, Long systemClockUptimeMillis) {
        List<Gesture> gesturesToMove = new ArrayList<Gesture>();

        for (Gesture gesture : pressedGestures) {
            // Flinging gestures should never move to their last coordinate before releasing the touch.
            if (gesture.shouldMove(currentTime)) {
                gesturesToMove.add(gesture);
            }
        }

        int gestureLength = gesturesToMove.size();

        if (gestureLength > 0) {
            MotionEvent motionEvent = obtainMotionEvent(gesturesToMove, MotionEvent.ACTION_MOVE, currentTime, findAbsoluteStartTime(), systemClockUptimeMillis);
            sendPointerSync(motionEvent);
        }
    }

    private long findAbsoluteStartTime() {
        long startTime = SystemClock.uptimeMillis();

        for (Gesture gesture : pressedGestures) {
            if (gesture.getAbsoluteDownTime() != null) {
                startTime = Math.min(gesture.getAbsoluteDownTime(), startTime);
            }
        }

        return startTime;
    }

    private long findEndTime() {
        long endTime = 0;

        for (Gesture gesture : gesturesToPerform) {
            endTime = Math.max(gesture.upEvent().getTime(), endTime);
        }

        return endTime;
    }

    public static void tryWaitForKeyguard(int timeoutSeconds) {
        KeyguardManager keyguardManager = (KeyguardManager)
                InstrumentationBackend.instrumentation.getContext().getSystemService(Context.KEYGUARD_SERVICE);

        if (!keyguardManager.inKeyguardRestrictedInputMode()) {
            return;
        }

        long startTime = SystemClock.uptimeMillis();

        while (SystemClock.uptimeMillis() - startTime <= timeoutSeconds * 1000) {
            if (!keyguardManager.inKeyguardRestrictedInputMode()) {
                break;
            }
        }

        // For now, if the keyguard has shown up once, we sleep a bit more.
        // TODO: Improve the implementation to detect focus of the activity to replace stopping when the keyguard is gone

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static MotionEvent obtainMotionEvent(List<Gesture> pressedGestures, int motionEventAction, long currentTime, Long absoluteDownTime) {
        return obtainMotionEvent(pressedGestures, motionEventAction, currentTime, absoluteDownTime, null);
    }

    public static MotionEvent obtainMotionEvent(List<Gesture> pressedGestures, int motionEventAction, long currentTime, Long absoluteDownTime, Long systemClockUptimeMillis) {
        int gestureLength = pressedGestures.size();

        Coordinate[] coordinates = new Coordinate[gestureLength];
        int[] pointerIds = new int[gestureLength];

        for (int i = 0; i < gestureLength; i++) {
            Gesture gesture = pressedGestures.get(i);
            coordinates[i] = gesture.getPosition(currentTime);
            pointerIds[i] = gesture.getId();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[gestureLength];

            for (int i = 0; i < gestureLength; i++) {
                MotionEvent.PointerCoords pointerCoord = new MotionEvent.PointerCoords();
                pointerCoord.x = coordinates[i].getX();
                pointerCoord.y = coordinates[i].getY();
                pointerCoord.pressure = 1;
                pointerCoord.size = 1;
                pointerCoords[i] = pointerCoord;
            }

            if (systemClockUptimeMillis == null) {
                systemClockUptimeMillis = SystemClock.uptimeMillis();
            }

            if (absoluteDownTime == null) {
                absoluteDownTime = systemClockUptimeMillis;
            }

            return MotionEvent.obtain(absoluteDownTime, systemClockUptimeMillis, motionEventAction, gestureLength, pointerIds, pointerCoords, 0, 1, 1, 0, 0, 0, 0);
        } else {
            try {
                Method method = MotionEvent.class.getMethod("obtainNano", long.class, long.class, long.class, int.class, int.class, int[].class, float[].class, int.class, float.class, float.class, int.class, int.class);
                float[] inData = new float[4*(gestureLength)];

                for (int i = 0; i < gestureLength; i++) {
                    inData[i*4] = coordinates[i].getX();
                    inData[i*4+1] = coordinates[i].getY();
                    inData[i*4+2] = 1.0f;
                    inData[i*4+3] = 1.0f;
                }

                if (systemClockUptimeMillis == null) {
                    systemClockUptimeMillis = SystemClock.uptimeMillis();
                }

                if (absoluteDownTime == null) {
                    absoluteDownTime = systemClockUptimeMillis;
                }

                return (MotionEvent)method.invoke(null, absoluteDownTime, systemClockUptimeMillis, systemClockUptimeMillis * 1000000, motionEventAction, gestureLength, pointerIds, inData, 0, 1, 1, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private class Gesture {
        List<Event> events;
        Integer id;
        Long absoluteDownTime;
        long timeOffset;
        boolean fling;

        public Gesture() {
            this(0);
        }

        public Gesture(long timeOffset) {
            events = new ArrayList<Event>();
            id = null;
            absoluteDownTime = null;
            this.timeOffset = timeOffset;
            setFling(false);
        }

        public void addTimeOffset(long timeOffset) {
            this.timeOffset += timeOffset;
        }

        public long getTimeOffset() {
            return timeOffset;
        }

        public Integer getId() {
            return id;
        }

        public boolean getFling() {
            return fling;
        }

        public Long getAbsoluteDownTime() {
            return absoluteDownTime;
        }

        public void setFling(boolean fling) {
            this.fling = fling;
        }

        public void addEvent(Event event) {
            addEvent(event, true);
        }

        public void addEvent(Event event, boolean useOffset) {
            if (upEvent() != null) {
                event.setTime(event.getTime() + upEvent().getTime());
            }

            if (useOffset) {
                event.setTime(event.getTime() + timeOffset);
            }

            events.add(event);
            timeOffset = 0;
        }

        public boolean hasEvents() {
            return (events != null && events.size() != 0);
        }

        public Event downEvent() {
            if (!hasEvents()) {
                return null;
            } else {
                return events.get(0);
            }
        }

        public Event upEvent() {
            if (!hasEvents()) {
                return null;
            } else {
                return events.get(events.size() - 1);
            }
        }

        public Pair<Event,Event> getEventPair(long time) {
            if (downEvent().getTime() == time) {
                return new Pair<Event,Event>(downEvent(), events.get(1));
            }

            int i;

            for (i = 0; i < events.size(); i++) {
                if (events.get(i).getTime() > time) {
                    break;
                }
            }

            if (i == 0) {
                return null;
            }

            return new Pair<Event,Event>(events.get(i-1), events.get(i));
        }

        public boolean shouldPress(long time) {
            return (getEventPair(time) != null);
        }

        public boolean shouldRelease(long time) {
            return (time >= upEvent().getTime());
        }

        public boolean shouldMove(long time) {
            if (!getFling() || time >= upEvent().getTime()) {
                return true;
            }

            Coordinate position = getPosition(time);
            Coordinate upCoordinate = upEvent().getCoordinate();
            double fullDistance = getEventPair(time).first.getCoordinate().distance(upCoordinate);
            double distance = position.distance(upCoordinate);

            return (distance / fullDistance > 0.05);
        }

        public Coordinate getPosition(long time) {
            if (time >= upEvent().getTime()) {
                return upEvent().getCoordinate();
            }

            Pair<Event,Event> eventPair = getEventPair(time);

            long startTime = eventPair.first.getTime();
            long endTime = eventPair.second.getTime();
            Coordinate from = eventPair.first.getCoordinate();
            Coordinate to = eventPair.second.getCoordinate();

            if (endTime == startTime) {
                return to;
            }

            float fraction = (float) (time - startTime)/(endTime - startTime);

            if (fraction == 0.0f) {
                return from;
            } else {
                return from.between(to, fraction);
            }
        }

        public MotionEvent.PointerProperties getPointerProperty() {
            MotionEvent.PointerProperties pointerProperties = new MotionEvent.PointerProperties();
            pointerProperties.id = id;
            pointerProperties.toolType = MotionEvent.TOOL_TYPE_FINGER;

            return pointerProperties;
        }

        public MotionEvent.PointerCoords getPointerCoord(long time) {
            Coordinate position = getPosition(time);
            MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
            pointerCoords.x = position.getX();
            pointerCoords.y = position.getY();
            pointerCoords.pressure = 1.0f;
            pointerCoords.size = 1.0f;

            return pointerCoords;
        }

        public void setId(List<Gesture> pressedGestures) {
            // The id of the pointer is 0-based. The id should always be as low as possible
            int id = 0;
            boolean idSet = false;

            while (!idSet) {
                idSet = true;

                for (Gesture gesture : pressedGestures) {
                    if (gesture.getId() == id) {
                        idSet = false;
                        id++;
                    }
                }
            }

            this.id = id;
        }

        public MotionEvent generateDownEvent(List<Gesture> pressedGestures) {
            int gestureLength = pressedGestures.size();
            setId(pressedGestures);
            int motionEventAction;

            // The down action is based on the amount of gestures currently down, not its own id/timing.
            if (gestureLength > 0) {
                motionEventAction = (gestureLength << MotionEvent.ACTION_POINTER_INDEX_SHIFT) + MotionEvent.ACTION_POINTER_DOWN;
            } else {
                motionEventAction = MotionEvent.ACTION_DOWN;
            }

            long time = downEvent().getTime();

             // Because this gesture is not yet down, it is not included in the list of gestures passed as the first parameter
            List<Gesture> allPressedGestures = new ArrayList<Gesture>(pressedGestures);
            allPressedGestures.add(this);

            absoluteDownTime = SystemClock.uptimeMillis();

            return obtainMotionEvent(allPressedGestures, motionEventAction, time, absoluteDownTime);
        }

        public MotionEvent generateUpEvent(List<Gesture> pressedGestures) {
            return generateUpEvent(pressedGestures, null);
        }

        public MotionEvent generateUpEvent(List<Gesture> pressedGestures, Long systemClockUptimeMillis) {
            int gestureLength = pressedGestures.size();
            int motionEventAction;
            int indexInPressedGestures = pressedGestures.indexOf(this);

            // The up action is based on the amount of gestures currently down, not its own id/timing.
            if (gestureLength > 1) {
                motionEventAction = (indexInPressedGestures << MotionEvent.ACTION_POINTER_INDEX_SHIFT) + MotionEvent.ACTION_POINTER_UP;
            } else {
                motionEventAction = MotionEvent.ACTION_UP;
            }

            long time = upEvent().getTime();

            return obtainMotionEvent(pressedGestures, motionEventAction, time, getAbsoluteDownTime(), systemClockUptimeMillis);
        }

        public String toString() {
            return "Gesture {Events: "+events.toString()+"}";
        }
    }

    private class Event {
        private Coordinate coordinate;
        private Long time;

        public Event() {
            this(null, null);
        }

        public Event(Coordinate coordinate, Long time) {
            setCoordinate(coordinate);
            setTime(time);
        }

        public Event copy() {
            return new Event(coordinate.copy(), getTime());
        }

        public Coordinate getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        public long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        public String toString() {
            return "Event {time: "+getTime()+", coordinate: "+getCoordinate()+"}";
        }
    }

    private class Coordinate {
        private int x;
        private int y;

        public Coordinate(int x, int y) {
            setX(x);
            setY(y);
        }

        public Coordinate copy() {
            return new Coordinate(getX(), getY());
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Coordinate between(Coordinate to, float fraction) {
            int x = (int) ((to.getX() - getX()) * fraction + getX());
            int y = (int) ((to.getY() - getY()) * fraction + getY());

            return new Coordinate(x, y);
        }

        public double distance(Coordinate to) {
            float xDistance = getX() - to.getX();
            float yDistance = getY() - to.getY();

            return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
        }

        public String toString() {
            return "Coordinate {x: "+getX()+", y: "+getY()+"}";
        }

        public boolean equals(Object object, double precision) {
            if (!(object instanceof Coordinate)) {
                return false;
            }

            Coordinate coordinate = (Coordinate)object;

            return (distance(coordinate) <= precision);
        }
    }
}