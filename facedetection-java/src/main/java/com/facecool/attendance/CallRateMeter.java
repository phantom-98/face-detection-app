package com.facecool.attendance;

import android.util.Log;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CallRateMeter {
    private static final ConcurrentHashMap<String, AtomicInteger> callCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicLong> lastTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicLong> lastLogTimes = new ConcurrentHashMap<>();
    private static final String TAG = "CallRateMeter";
    private static final long LOG_INTERVAL = 5000;

    public static void measureCallRate() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        measureCallRateWithSpecificKey(caller, null);
    }

    public static void measureCallRateInsideLoop(Object specificKey) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        measureCallRateWithSpecificKey(caller, specificKey);
    }

    private static void measureCallRateWithSpecificKey(StackTraceElement caller, Object specificKey) {
        String threadName = Thread.currentThread().getName();
        String fullClassName = caller.getClassName();
        String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        String methodName = caller.getMethodName();
        int lineNumber = caller.getLineNumber();
        String key = threadName + ":" + simpleClassName + "." + methodName + ":" + lineNumber;

        if (specificKey != null) {
            key += ":" + specificKey.toString();
        }

        callCounts.putIfAbsent(key, new AtomicInteger(0));
        lastTimes.putIfAbsent(key, new AtomicLong(System.currentTimeMillis()));
        lastLogTimes.putIfAbsent(key, new AtomicLong(0));

        int calls = callCounts.get(key).incrementAndGet();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastTimes.get(key).get();
        long elapsedTime = currentTime - lastTime;
        long lastLogTime = lastLogTimes.get(key).get();
        long elapsedLogTime = currentTime - lastLogTime;

        if (elapsedTime >= 1000 && (elapsedLogTime >= LOG_INTERVAL || lastLogTime == 0)) {
            double rate = (double) calls / (elapsedTime / 1000.0);
            Log.i(TAG, "[" + key + "] Rate: " + rate + " calls/sec");

            callCounts.get(key).set(0);
            lastTimes.get(key).set(currentTime);
            lastLogTimes.get(key).set(currentTime);
        }
    }
}