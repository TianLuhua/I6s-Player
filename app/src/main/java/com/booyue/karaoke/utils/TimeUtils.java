package com.booyue.karaoke.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/9/4.20:12
 */

public class TimeUtils {

    public static String formatTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date(time));
    }
}
