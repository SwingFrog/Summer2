package com.swingfrog.summer2.starter.event;

/**
 * @author: toke
 */
public interface SummerListener {

    int PRIORITY_SYSTEM = 99999;
    int PRIORITY_HIGH = 66666;
    int PRIORITY_MIDDLE = 33333;
    int PRIORITY_LOW = 11111;
    int PRIORITY_DEFAULT = 0;

    default int priority() { return PRIORITY_DEFAULT; }
    default void onPrepare(SummerContext context) {}
    void onStart(SummerContext context);
    void onStop(SummerContext context);
    default void onDestroy(SummerContext context) {}

}
