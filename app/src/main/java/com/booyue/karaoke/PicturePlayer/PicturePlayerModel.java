package com.booyue.karaoke.PicturePlayer;

import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.booyue.karaoke.base.BaseModel;
import com.booyue.karaoke.utils.ThreadPoolManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.callback.Callback;


/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class PicturePlayerModel implements BaseModel {

    private CallBack callback;
    private List<String> imageInfoList = new ArrayList<>();
    private int position;

    public PicturePlayerModel(CallBack callBack) {
        this.callback = callBack;
    }

    public void getData(final Uri uri) {
        ThreadPoolManager.newInstance().addExecuteTask(new Runnable() {
            @Override
            public void run() {
                String path = uri.getPath();
                int startIndex = path.lastIndexOf("/");
                File rootFile = new File(path.substring(0, startIndex));
                String rootPath = rootFile.getPath();
                imageInfoList.clear();
                for (String s : rootFile.list()) {
                    String childPath = rootPath + "/" + s;
                    //系统支持：jpg、png
                    if (childPath.endsWith(".jpg") || childPath.endsWith(".png")) {
                        imageInfoList.add(childPath);
                    }
                }
                position = imageInfoList.indexOf(path) + 1;
                if (callback == null)
                    return;
                callback.setData(imageInfoList, position);
            }
        });

    }

    public void getData(final int i) {
        this.position = i;
        if (position >= imageInfoList.size() - 1)
            position = 0;
        if (position <= 0)
            position = imageInfoList.size() - 1;
        ThreadPoolManager.newInstance().addExecuteTask(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(imageInfoList.get(position));
                if (callback == null)
                    return;
                callback.setData(bitmap, getSimpleName(), imageInfoList.size(), position);
            }
        });

    }

    private String getSimpleName() {
        String simpleNam = "";
        String videoPath = imageInfoList.get(position);
        int startIndex = videoPath.lastIndexOf("/");
        int endIndex = videoPath.lastIndexOf(".");
        simpleNam = videoPath.substring(startIndex + 1, endIndex);
        return simpleNam;
    }

    public int getCureentPosition() {
        return position;
    }

    @Override
    public void onDestroy() {
        if (imageInfoList != null)
            imageInfoList = null;
        if (callback != null)
            callback = null;
    }

    interface CallBack {

        void setData(List<String> imageInfoList, int position);

        void setData(Bitmap bitmap, String name, int total, int position);
    }


}
