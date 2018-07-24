package com.booyue.karaoke.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.booyue.karaoke.adapter.MyGridViewAdapter;

/**
 * Created by Administrator on 2017/9/4.18:05
 */

public class VideoUtils {
    private static final String TAG = "VideoUtils--";

    public void getImageFormVideo(MyGridViewAdapter.ReqeustBitmap reqeustBitmap, String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
            LoggerUtils.d(TAG + filePath);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        if(reqeustBitmap != null){
            reqeustBitmap.onBitmapCallback(filePath,bitmap);
        }
    }
}
