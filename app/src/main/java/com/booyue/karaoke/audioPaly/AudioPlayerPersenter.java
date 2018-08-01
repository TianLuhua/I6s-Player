package com.booyue.karaoke.audioPaly;

import android.content.Context;
import android.net.Uri;

import com.booyue.karaoke.audioPaly.bean.AudioBean;
import com.booyue.karaoke.base.AbstractPresenter;

import java.util.List;


/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class AudioPlayerPersenter extends AbstractPresenter<AudioPlayerView> {

    private AudioPlayerModel model;
    private boolean isLoopPlay = false;

    private int position;
    private List<AudioBean> imageInfoList;


    public AudioPlayerPersenter(Context mContext) {
        model = new AudioPlayerModel(mContext, new AudioPlayerModel.CallBack() {
            @Override
            public void setData(List<AudioBean> imageInfoList, int position) {
                AudioPlayerView view = getView();
                if (view == null)
                    return;
                view.setData(imageInfoList, position);
            }
        });
        this.position = model.getCureentPosition();
        this.imageInfoList = model.getAudioInfos();
    }

    public void getData(Uri uri) {
        if (model == null)
            return;
        model.getData(uri);
    }


    public void setCureentPage(int position) {
        if (model == null) return;
        this.position = position;
    }


    /**
     * 上一曲
     */
    public void onPrev() {
        position--;
        if (position < 0) {
            position = imageInfoList.size() - 1;
        }
        getView().startPlay(imageInfoList.get(position).getPath(), position);
    }

    /**
     * 下一曲
     */
    public void onNext() {
        position++;
        if (position >= imageInfoList.size()) {
            position = 0;
        }
        getView().startPlay(imageInfoList.get(position).getPath(), position);
    }

    /**
     * 根据用户设置来单个循环还是循环list列表播放
     */
    public void startPlayAccordingToPlayMode() {
        if (AudioController.STATE_PLAY_MODE == AudioController.STATE_PLAY_MODE_SINGLE)
            getView().startPlay(imageInfoList.get(position).getPath(), position);
        else
            onNext();
    }

    @Override
    public void detachView() {
        super.detachView();
        if (model != null)
            model.onDestroy();
    }
}
