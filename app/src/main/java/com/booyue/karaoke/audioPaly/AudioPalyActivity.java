package com.booyue.karaoke.audioPaly;

import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.booyue.karaoke.R;
import com.booyue.karaoke.audioPaly.bean.AudioBean;
import com.booyue.karaoke.base.AbstractMVPActivity;

import java.util.List;


/**
 * Created by Tianluhua on 2018\8\1 0001.
 */
public class AudioPalyActivity extends AbstractMVPActivity<AudioPlayerView, AudioPlayerPersenter> implements AudioPlayerView, View.OnClickListener {


    private AudioController controller;
    private ImageView bgAudioPlay;
    private ImageView back;

    private MediaPlayer  mMediaPlayer;


    @Override
    public void setData(List<AudioBean> imageInfoList, int position) {

    }

    @Override
    public void setCureentPage(int page, int palyModle) {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_audio;
    }

    @Override
    protected void initView() {
        back = findViewById(R.id.action_bar_back);
        back.setOnClickListener(this);
        bgAudioPlay = findViewById(R.id.layout_play);
        bgAudioPlay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                controller.show();
                bgAudioPlay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        controller = new AudioController(getApplicationContext());
        controller.setAnchorView(bgAudioPlay);
        mMediaPlayer=new MediaPlayer();
        controller.setMediaPlayer(mMediaPlayer);
    }


    @Override
    protected AudioPlayerPersenter createPresenter() {
        return new AudioPlayerPersenter(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_bar_back:
                finish();
                break;
        }
    }
}
