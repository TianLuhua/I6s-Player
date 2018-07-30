package com.booyue.karaoke.PicturePlayer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.IccOpenLogicalChannelResponse;
import android.util.Log;

import com.booyue.karaoke.base.AbstractPresenter;

import java.util.List;

/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class PicturePlayerPersenter extends AbstractPresenter<PicturePlayerView> {

    private PicturePlayerModel model;

    public int PLAYMODLE_SINGLE = 0x00001;
    public int PLAYMODLE_LOOP = 0x00002;


    private int CURRENT_PLAYMODLE = PLAYMODLE_SINGLE;

    private boolean isLoopPlay = false;
    public static final int LOOP_PLAY = 0x0003;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOOP_PLAY:
                    int position = model.getCureentPosition();
                    getData(position++);
                    loop();
                    break;

            }
        }
    };

    public PicturePlayerPersenter() {
        model = new PicturePlayerModel(new PicturePlayerModel.CallBack() {
            @Override
            public void setData(List<String> imageInfoList, int position) {
                PicturePlayerView view = getView();
                if (view == null)
                    return;
                view.setData(imageInfoList, position);
            }

            @Override
            public void setData(Bitmap bitmap, String name, int total, int position) {
                PicturePlayerView view = getView();
                if (view == null)
                    return;
                view.setData(bitmap, name, total, position);
            }
        });
    }

    public void getData(Uri uri) {
        if (model == null)
            return;
        model.getData(uri);
    }

    public void getData(int i) {
        if (model == null)
            return;
        Log.e("PicturePlayerPersenter", "position:"+i);
        model.getData(i);

    }

    public void changPlayModle() {

        if (CURRENT_PLAYMODLE == PLAYMODLE_SINGLE) {
            CURRENT_PLAYMODLE = PLAYMODLE_LOOP;
            isLoopPlay = true;
            loop();
            return;
        }

        if (CURRENT_PLAYMODLE == PLAYMODLE_LOOP) {
            CURRENT_PLAYMODLE = PLAYMODLE_SINGLE;
            isLoopPlay = false;
            return;
        }
    }

    private void loop() {
        if (isLoopPlay) {
            mHandler.removeMessages(LOOP_PLAY);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(LOOP_PLAY), 2000);
        }
    }


}
