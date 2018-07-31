package com.booyue.karaoke.PicturePlayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.booyue.karaoke.base.AbstractPresenter;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

import static com.booyue.karaoke.PicturePlayer.PicturePlayController.PLAYMODLE_LOOP;
import static com.booyue.karaoke.PicturePlayer.PicturePlayController.PLAYMODLE_SINGLE;

/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class PicturePlayerPersenter extends AbstractPresenter<PicturePlayerView> {

    public static final int LOOP_PLAY = 0x0006;
    private int CURRENT_PLAYMODLE = PLAYMODLE_SINGLE;
    private int loopTime = 3000;
    private PicturePlayerModel model;
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

    public PicturePlayerPersenter(Context mContext) {
        model = new PicturePlayerModel(mContext, new PicturePlayerModel.CallBack() {
            @Override
            public void setData(List<PhotoView> imageInfoList, int position) {
                PicturePlayerView view = getView();
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
        if (model == null) return ;
        this.position=position;
    }
}
