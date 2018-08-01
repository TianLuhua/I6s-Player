package com.booyue.karaoke.audioPaly;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.booyue.karaoke.audioPaly.bean.AudioBean;
import com.booyue.karaoke.base.AbstractPresenter;

import java.util.List;

import static com.booyue.karaoke.pictureplayer.PicturePlayController.PLAYMODLE_LOOP;
import static com.booyue.karaoke.pictureplayer.PicturePlayController.PLAYMODLE_SINGLE;

/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class AudioPlayerPersenter extends AbstractPresenter<AudioPlayerView> {

    public static final int LOOP_PLAY = 0x0006;
    private int CURRENT_PLAYMODLE = PLAYMODLE_SINGLE;
    private int loopTime = 3000;
    private AudioPlayerModel model;
    private boolean isLoopPlay = false;
    private int position;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOOP_PLAY:
                    if (getView() == null)
                        return;
                    if (position > model.getDataSizi() - 1)
                        position = 0;
                    getView().setCureentPage(position, CURRENT_PLAYMODLE);
                    loopPlay();
                    break;

            }
        }
    };

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
        position = model.getCureentPosition();
    }

    public void getData(Uri uri) {
        if (model == null)
            return;
        model.getData(uri);
    }

    public void setLoopTime(int loopTime) {
        this.loopTime = loopTime;
    }

    public void changPlayModle() {
        if (CURRENT_PLAYMODLE == PLAYMODLE_SINGLE) {
            CURRENT_PLAYMODLE = PLAYMODLE_LOOP;
            isLoopPlay = true;
            loopPlay();
            return;
        }
        if (CURRENT_PLAYMODLE == PLAYMODLE_LOOP) {
            CURRENT_PLAYMODLE = PLAYMODLE_SINGLE;
            isLoopPlay = false;
            singlePlay();
            return;
        }
    }

    private void loopPlay() {
        mHandler.removeMessages(LOOP_PLAY);
        if (isLoopPlay) {
            getView().setCureentPage(position, CURRENT_PLAYMODLE);
            position++;
            mHandler.sendMessageDelayed(mHandler.obtainMessage(LOOP_PLAY), loopTime);
        }
    }

    private void singlePlay() {
        if (!isLoopPlay) {
            getView().setCureentPage(position, CURRENT_PLAYMODLE);
        }
    }

    public String getImagePaths(int currentItem) {
        if (model == null) return "";
        return model.getImagePaths().get(currentItem);
    }

    public void setCureentPage(int position) {
        if (model == null) return;
        this.position = position;
    }

    @Override
    public void detachView() {
        super.detachView();
        if (model != null)
            model.onDestroy();
        ;
    }
}
