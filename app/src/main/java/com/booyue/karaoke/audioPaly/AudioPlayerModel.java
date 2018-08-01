package com.booyue.karaoke.audioPaly;

import android.content.Context;
import android.net.Uri;

import com.booyue.karaoke.audioPaly.bean.AudioBean;
import com.booyue.karaoke.base.BaseModel;
import com.booyue.karaoke.utils.ThreadPoolManager;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class AudioPlayerModel implements BaseModel {

    private CallBack callback;
    private List<AudioBean> audioInfoList = new ArrayList<>();
    private List<String> audioPaths = new ArrayList<>();
    private int position;
    private Context mContext;


    public AudioPlayerModel(Context mContext, CallBack callBack) {
        this.callback = callBack;
        this.mContext = mContext;
    }

    public void getData(final Uri uri) {
        ThreadPoolManager.newInstance().addExecuteTask(new Runnable() {
            @Override
            public void run() {
                String path = uri.getPath();
                int startIndex = path.lastIndexOf("/");
                File rootFile = new File(path.substring(0, startIndex));
                String chooseFilePath = path.substring(startIndex + 1, path.length());
                String rootPath = rootFile.getPath();
                audioInfoList.clear();
                audioPaths.clear();
                for (String s : rootFile.list()) {
                    String childPath = rootPath + "/" + s;
                    //系统支持：mp3
                    if (childPath.endsWith(".mp3")) {
                        audioPaths.add(s);
                        AudioBean audio = new AudioBean();
                        audio.setName(s);
                        audio.setPath(childPath);
                        audio.setPlaying(false);
                        audioInfoList.add(audio);
                    }
                }
                position = audioPaths.indexOf(chooseFilePath);
                if (callback == null)
                    return;
                callback.setData(audioInfoList, position);
            }
        });

    }


    public int getCureentPosition() {
        return position;
    }

    public List<AudioBean> getAudioInfos() {
        return audioInfoList;
    }

    @Override
    public void onDestroy() {
        if (audioInfoList != null)
            audioInfoList = null;
        if (callback != null)
            callback = null;
        if (audioPaths != null)
            audioPaths = null;
        if (mContext != null)
            mContext = null;
    }

    public int getDataSizi() {
        return audioInfoList.size();
    }

    interface CallBack {
        void setData(List<AudioBean> audioInfoLists, int position);
    }


}
