package com.booyue.karaoke.utils;

import android.util.Log;

import com.booyue.karaoke.bean.Constants;

/**
 * Created by Administrator on 2017/9/4.17:55
 */

public class Logger {
    private static final String TAG = "Logger------";
    public static void i(String TAG, String text) {
        if(Constants.IS_DEBUG) {
            Log.i(TAG, text);
        }
    }

    public static void d(String tag, String text) {
        if(Constants.IS_DEBUG) {
            Log.d(TAG, tag + "------" + text);
        }
    }

    public static void v(String TAG, String text) {
        if(Constants.IS_DEBUG) {
            Log.v(TAG, text);
        }
    }

    public static void w(String TAG, String text) {
        if(Constants.IS_DEBUG) {
            Log.w(TAG, text);
        }
    }

    public static void e(String tag, String text) {
        if(Constants.IS_DEBUG) {
            Log.e(TAG, tag + "------" + text);
        }
    }
}
