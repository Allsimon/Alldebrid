package com.malek.alldebrid.utils;

import android.util.Log;

public class Logg {
    private static long timestamp;

    public static void e(Object o, Object o2) {
        Log.e(o.toString(), o2.toString());
    }

    public static void e(Object o) {
        Log.e(getLineCalled(), o.toString());
    }

    public static void e() {
        Log.e(getLineCalled(), "");
    }

    public static void ping() {
        timestamp = System.currentTimeMillis();
    }

    public static void pong() {
        Log.e(getLineCalled(), "Time since last ping " + (System.currentTimeMillis() - timestamp));
    }

    private static String getLineCalled() {
        String fullClassName = Thread.currentThread().getStackTrace()
                [4].getClassName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[4].getMethodName();
        int lineNumber = Thread.currentThread().getStackTrace()[4].getLineNumber();
        return className + "." + methodName + "():" + lineNumber;
    }

    public static void printFullTrace() {
        String fullClassName;
        String className;
        String methodName;
        int lineNumber;
        for (int i = 0; i < Thread.currentThread().getStackTrace().length; i++) {
            fullClassName = Thread.currentThread().getStackTrace()[i].getClassName();
            className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            methodName = Thread.currentThread().getStackTrace()[i].getMethodName();
            lineNumber = Thread.currentThread().getStackTrace()[i].getLineNumber();
            Log.e("", className + "." + methodName + "():" + lineNumber);
        }
    }

    public static void d(Object o, Object o2) {
        Log.d(o.toString(), o2.toString());
    }

    public static void d(Object o) {
        Log.d(o.toString(), o.toString());
    }
}