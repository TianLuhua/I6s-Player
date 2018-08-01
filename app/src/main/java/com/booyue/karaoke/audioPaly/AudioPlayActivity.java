package com.booyue.karaoke.audioPaly;

import android.media.MediaPlayer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.booyue.karaoke.R;
import com.booyue.karaoke.audioPaly.adapter.AudioListAdapter;
import com.booyue.karaoke.audioPaly.bean.AudioBean;
import com.booyue.karaoke.base.AbstractMVPActivity;

import java.io.IOException;
import java.util.List;


/**
 * Created by Tianluhua on 2018\8\1 0001.
 */
public class AudioPlayActivity extends AbstractMVPActivity<AudioPlayerView, AudioPlayerPersenter> implements
        AudioPlayerView,
        View.OnClickListener,
        AudioController.AudioPlayerUIListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnCompletionListener,
        AudioListAdapter.OnItemClickListener {

    //播放部分
    private AudioController controller;
    private ImageView bgAudioPlay;
    private ImageView back;
    private MediaPlayer mMediaPlayer;

    //列表部分
    private RecyclerView audioList;
    private AudioListAdapter adapter;

    @Override
    protected int getContentViewID() {
        return R.layout.activity_audio;
    }

    @Override
    protected void initView() {
        back = findViewById(R.id.action_bar_back);
        back.setOnClickListener(this);
        bgAudioPlay = findViewById(R.id.layout_play);
        //这里显示poupWindow时序问题需要注意，必须在视图加载完毕了才能显示poupWinod
        bgAudioPlay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                controller.show();
                bgAudioPlay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        controller = new AudioController(getApplicationContext());
        controller.setMediaPlayerUIListener(this);
        controller.setAnchorView(bgAudioPlay);
        mMediaPlayer = new MediaPlayer();
        controller.setMediaPlayer(mMediaPlayer);
        //Init Listener
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnInfoListener(this);

        //初始数据列表
        audioList = findViewById(R.id.layout_list);
        audioList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AudioListAdapter(getApplicationContext());
        adapter.setOnItemClickListener(this);
        audioList.setAdapter(adapter);

        //请求数据
        getPresenter().getData(getIntent().getData());

    }

    @Override
    public void setData(final List<AudioBean> audioInfoList, final int position) {
        AudioBean audioBean = audioInfoList.get(position);
        audioBean.setPlaying(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //显示数据
                notficationDataChange(audioInfoList, position);
            }
        });
        startPlayAudio(audioBean.getPath());
    }

    @Override
    public void startPlay(String uri, int position) {
        startPlayAudio(uri);
        notficationDataChange(position);
    }

    /**
     * 通知adapter 数据状态发生了变化
     *
     * @param audioInfoList
     * @param position
     */
    private void notficationDataChange(final List<AudioBean> audioInfoList, final int position) {
        adapter.setAudioBeans(audioInfoList, position);
        //滚动到指定的位置
        notficationDataChange(position);
    }

    private void notficationDataChange(final int position) {
        adapter.setCureentPlayingItem(position);
        audioList.scrollToPosition(position);
    }


    /**
     * 开始播放
     *
     * @param uri 音乐路径
     */
    private void startPlayAudio(String uri) {
        try {
            if (mMediaPlayer == null)
                return;
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(uri);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("AudioPlayActivity", e.getMessage());
        }
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

    @Override
    public void onPrev() {
        getPresenter().onPrev();

    }

    @Override
    public void onNext() {
        getPresenter().onNext();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        getPresenter().startPlayAccordingToPlayMode();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller.isShowing()) {
            controller.hide();
        }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }
    }

    @Override
    public void onItemClick(AudioBean audioBean, int position) {
        startPlayAudio(audioBean.getPath());
        audioBean.setPlaying(true);
        notficationDataChange(position);
        //让用户操作和业务逻辑同步
        getPresenter().setCureentPage(position);
    }
}
