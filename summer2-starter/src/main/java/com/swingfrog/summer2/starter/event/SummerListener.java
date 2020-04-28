package com.swingfrog.summer2.starter.event;

/**
 * @author: toke
 */
public interface SummerListener {

    default void onPrepare(SummerContext context) {}
    void onStart(SummerContext context);
    void onStop(SummerContext context);

}
