package com.booyue.karaoke.bean;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2017/9/4.17:56
 */

public class Constants {
    public static boolean IS_DEBUG = false;
    public static String SD_PATH_YIHENGKE = "/oem/";//一恒科目录更改成/oem/
    public static String KARAOKE_PATH_YIHENGKE = SD_PATH_YIHENGKE + "karaoke";
    public static String MTV_PATH_YIHENGKE = SD_PATH_YIHENGKE + "mtv";

    public static String SD_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator;
    public static String KARAOKE_PATH = SD_PATH + "karaoke";
    public static String MTV_PATH = SD_PATH + "mtv";
//    public static String MTV_PATH = SD_PATH + "卡拉OK" + File.separator + "兔兔MTV";
//    public static String VIDEO_PATH = SD_PATH + "videos";

}
