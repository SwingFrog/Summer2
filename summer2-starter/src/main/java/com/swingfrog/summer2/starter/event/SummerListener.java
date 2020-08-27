package com.swingfrog.summer2.starter.event;

/**
 * @author: toke
 */
public interface SummerListener {

    int PRIORITY_SYSTEM = 1000000;
    int PRIORITY_HIGH = 100000;
    int PRIORITY_MIDDLE = 10000;
    int PRIORITY_LOW = 1000;
    int PRIORITY_DEFAULT = 0;

    default int priority() { return PRIORITY_DEFAULT; }
    default void onPrepare(SummerContext context) {}
    void onStart(SummerContext context);
    void onStop(SummerContext context);
    default void onDestroy(SummerContext context) {}

}
