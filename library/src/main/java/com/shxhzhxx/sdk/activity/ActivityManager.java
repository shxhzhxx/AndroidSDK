package com.shxhzhxx.sdk.activity;

//import android.app.Activity;
//
//import java.lang.ref.WeakReference;
//import java.util.Stack;

public abstract class ActivityManager {
//    private static Stack<WeakReference<Activity>> mActivities = new Stack<>();
//
//    public static void add(Activity activity) {
//        if (activity == null)
//            return;
//        for (int i = 0; i < mActivities.size(); ) {
//            Activity a = mActivities.get(i).get();
//            if (a == null) {
//                mActivities.remove(i);
//            } else {
//                if (a == activity)
//                    return;
//                ++i;
//            }
//        }
//        mActivities.add(new WeakReference<>(activity));
//    }
//
//    public static void remove(Activity activity) {
//        for (int i = 0; i < mActivities.size(); ) {
//            Activity a = mActivities.get(i).get();
//            if (a == null) {
//                mActivities.remove(i);
//            } else {
//                if (a == activity) {
//                    mActivities.remove(i);
//                    return;
//                }
//                ++i;
//            }
//        }
//    }
//
//    public static Activity last() {
//        while (!mActivities.isEmpty()) {
//            WeakReference<Activity> ref = mActivities.lastElement();
//            Activity activity = ref.get();
//            if (activity == null) {
//                mActivities.remove(ref);
//            } else {
//                return activity;
//            }
//        }
//        return null;
//    }
//
//    public static void finishAll(Activity without) {
//        for (int i = 0; i < mActivities.size(); ) {
//            Activity a = mActivities.get(i).get();
//            if (a == null) {
//                mActivities.remove(i);
//            } else {
//                if (a != without) {
//                    mActivities.remove(i);
//                    a.finish();
//                } else {
//                    ++i;
//                }
//            }
//        }
//    }
}
