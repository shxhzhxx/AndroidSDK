package com.shxhzhxx.sdk.utils;

public abstract class ConditionRunnable implements Runnable{
    private boolean mEventA = false, mEventB = false;

    public void setEventA(boolean cond) {
        mEventA = cond;
        if (mEventA && mEventB)
            run();
    }

    public void setEventB(boolean cond) {
        mEventB = cond;
        if (mEventA && mEventB)
            run();
    }

    public void reset() {
        mEventA = false;
        mEventB = false;
    }
}
