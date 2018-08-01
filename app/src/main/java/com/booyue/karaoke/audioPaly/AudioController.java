/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 YIXIA.COM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.booyue.karaoke.audioPaly;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.booyue.karaoke.utils.DisplayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.R;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.OutlineTextView;

public class AudioController extends FrameLayout implements View.OnClickListener {
    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private MediaPlayer mPlayer;
    private Context mContext;
    private PopupWindow mWindow;
    private int mAnimStyle;
    private View mAnchor;
    private View mRoot;
    private OutlineTextView mInfoView;
    private long mDuration;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mInstantSeeking = false;
    private boolean mFromXml = false;

    private AudioManager mAM;
    private OnShownListener mShownListener;
    private OnHiddenListener mHiddenListener;

    private ImageView ibPlay;
    private ImageView ibPlayMode;
    private ImageView ibPrev;
    private ImageView ibNext;

    private FrameLayout ivAccompany;
    private ImageView ivMediaVolume;
    private FrameLayout flVideo;

    private SeekBar mProgress;
    private TextView tvDuration, tvCurrentime;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
                case FADE_OUT:
//                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                        updatePausePlay();
                    }
                    break;
            }
        }
    };
    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
            show(3600000);
            mHandler.removeMessages(SHOW_PROGRESS);
            if (mInstantSeeking)
                mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);
            if (mInfoView != null) {
                mInfoView.setText("");
                mInfoView.setVisibility(View.VISIBLE);
            }
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser)
                return;
            long newposition = (mDuration * progress) / 1000;
            String time = StringUtils.generateTime(newposition);
            if (mInstantSeeking)
                mPlayer.seekTo((int) newposition);
            if (mInfoView != null)
                mInfoView.setText(time);
            if (tvCurrentime != null)
                tvCurrentime.setText(time);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (!mInstantSeeking)
                mPlayer.seekTo((int) ((mDuration * bar.getProgress()) / 1000));
            if (mInfoView != null) {
                mInfoView.setText("");
                mInfoView.setVisibility(View.GONE);
            }
            show(sDefaultTimeout);
            mHandler.removeMessages(SHOW_PROGRESS);
            mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mDragging = false;
            mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
        }
    };
    private SeekBar volume_progress;
    private TextView tvProgress;
    private LinearLayout accompanyLayout;
    private MyBroadcaseReceiver myBroadcaseReceiver;
    private ImageView ivOriginal;


    public AudioController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mFromXml = true;
        initController(context);
    }

    public AudioController(Context context) {
        super(context);
        if (!mFromXml && initController(context))
            initFloatingWindow();

    }

    public void initPlayModeIcon(boolean isSwitch) {
        if (isSwitch) {
            if (STATE_PLAY_MODE == STATE_PLAY_MODE_SINGLE) {
                ibPlayMode.setImageResource(R.drawable.mediacontorller_cycle);
                STATE_PLAY_MODE = STATE_PLAY_MODE_LIST;

            } else if (STATE_PLAY_MODE == STATE_PLAY_MODE_LIST) {
                ibPlayMode.setImageResource(R.drawable.mediacontorller_single);
                STATE_PLAY_MODE = STATE_PLAY_MODE_SINGLE;
            }
        } else {
            if (STATE_PLAY_MODE == STATE_PLAY_MODE_SINGLE) {
                ibPlayMode.setImageResource(R.drawable.mediacontorller_single);
            } else if (STATE_PLAY_MODE == STATE_PLAY_MODE_LIST) {
                ibPlayMode.setImageResource(R.drawable.mediacontorller_cycle);
            }
        }
    }

    private boolean initController(Context context) {
        mContext = context.getApplicationContext();
        mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return true;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initControllerView(mRoot);
    }

    private void initFloatingWindow() {
        mWindow = new PopupWindow(mContext);
        mWindow.setFocusable(false);
        mWindow.setBackgroundDrawable(null);
        mWindow.setOutsideTouchable(true);
        mAnimStyle = android.R.style.Animation;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setWindowLayoutType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                Method setWindowLayoutType = PopupWindow.class.getMethod("setWindowLayoutType", new Class[]{int.class});
                setWindowLayoutType.invoke(mWindow, WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
            } catch (Exception e) {
                Log.e("setWindowLayoutType", e);
            }
        }
    }

    public void setAnchorView(View view) {
        mAnchor = view;
        if (!mFromXml) {
            removeAllViews();
            mRoot = makeControllerView();
            mWindow.setContentView(mRoot);
            mWindow.setWidth(LayoutParams.WRAP_CONTENT);
            mWindow.setHeight(LayoutParams.WRAP_CONTENT);
        }
        initControllerView(mRoot);
    }

    protected View makeControllerView() {
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(getResources().getIdentifier("contorller_audio", "layout", mContext.getPackageName()), this);
    }

    private void initControllerView(View v) {

        tvCurrentime = getViewById(v, R.id.tv_currenttime_vitamio);
        tvDuration = getViewById(v, R.id.tv_durationtime_vitamio);

        ibPlayMode = getViewById(v, R.id.ib_playmode_vitamio);
        ibPrev = getViewById(v, R.id.ib_prev_vitamio);
        ibPlay = getViewById(v, R.id.ib_play_vitamio);
        ibNext = getViewById(v, R.id.ib_next_vitamio);
        ivMediaVolume = getViewById(v, R.id.img_media_volume);

        accompanyLayout = getViewById(v, R.id.fl_accompany);
        ivAccompany = getViewById(v, R.id.img_accompany);
        ivOriginal = getViewById(v, R.id.img_original);
        flVideo = getViewById(v, R.id.fl_video_vitamio);
        initListener();
        if (ibPlay != null) {
            ibPlay.requestFocus();
            ibPlay.setOnClickListener(mPauseListener);
        }

        mProgress = v.findViewById(R.id.sb_progress_vitamio);
        if (mProgress != null) {
            mProgress.setOnSeekBarChangeListener(mSeekListener);
            mProgress.setMax(1000);
        }
    }

    private void initListener() {
        ibPlayMode.setOnClickListener(this);
        ibPrev.setOnClickListener(this);
        ibNext.setOnClickListener(this);
    }

    public void setMediaPlayer(MediaPlayer player) {
        mPlayer = player;
        updatePausePlay();
    }

    /**
     * @param v   viewGroup
     * @param id  group 通过id找到对应的view
     * @param <T> view的类型
     * @return
     */
    private <T> T getViewById(View v, int id) {
        return (T) v.findViewById(id);
    }

    /**
     * Control the action when the seekbar dragged by user
     *
     * @param seekWhenDragging True the media will seek periodically
     */
    public void setInstantSeeking(boolean seekWhenDragging) {
        mInstantSeeking = seekWhenDragging;
    }

    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Set the content of the file_name TextView
     *
     * @param name
     */
    public void setFileName(String name) {
    }


    /**
     * Set the View to hold some information when interact with the
     * MediaController
     *
     * @param v
     */
    public void setInfoView(OutlineTextView v) {
        mInfoView = v;
    }

    public void setAnimationStyle(int animationStyle) {
        mAnimStyle = animationStyle;
    }

    /**
     * Show the controller on screen. It will go away automatically after
     * 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show the controller
     *                until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {
            if (ibPlay != null)
                ibPlay.requestFocus();
            /**modify by : 2017/11/8 18:12*/
            if (accompanyLayout != null && accompanyLayout.getVisibility() == GONE) {
                accompanyLayout.setVisibility(VISIBLE);
            }
//      disableUnsupportedButtons();

            if (mFromXml) {
                setVisibility(View.VISIBLE);
            } else {
                int[] location = new int[2];

                mAnchor.getLocationOnScreen(location);
                Log.d("show--->x:"+location[0]+",Y:"+location[1]);
                Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1] + mAnchor.getHeight());

                mWindow.setAnimationStyle(mAnimStyle);
//                setWindowLayoutType();
                mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, anchorRect.left+ DisplayUtils.px2dp(mContext,8), anchorRect.top);
            }
            mShowing = true;
            if (mShownListener != null)
                mShownListener.onShown();
        }
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void hide() {
        if (mAnchor == null)
            return;
//        if (volumeView.getVisibility() == VISIBLE) {
//            volumeView.setVisibility(GONE);
//        }

        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                if (mFromXml)
                    setVisibility(View.GONE);
                else
                    mWindow.dismiss();
            } catch (IllegalArgumentException ex) {
                Log.d("MediaController already removed");
            }
            mShowing = false;
            if (mHiddenListener != null)
                mHiddenListener.onHidden();
        }
    }

    public void setOnShownListener(OnShownListener l) {
        mShownListener = l;
    }

    public void setOnHiddenListener(OnHiddenListener l) {
        mHiddenListener = l;
    }

    private long setProgress() {
        if (mPlayer == null || mDragging)
            return 0;

        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
//            int percent = mPlayer.getBufferPercentage();
//            mProgress.setSecondaryProgress(percent * 10);
        }

        mDuration = duration;

        if (tvDuration != null)
            tvDuration.setText(StringUtils.generateTime(mDuration));
        if (tvCurrentime != null)
            tvCurrentime.setText(StringUtils.generateTime(position));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0 && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
            doPauseResume();
            show(sDefaultTimeout);
            if (ibPlay != null)
                ibPlay.requestFocus();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            hide();
            return true;
        } else {
            show(sDefaultTimeout);
        }
        return super.dispatchKeyEvent(event);
    }

    private void updatePausePlay() {
        if (mRoot == null || ibPlay == null)
            return;

        if (mPlayer.isPlaying())
            ibPlay.setImageResource(R.drawable.mediacontorller_pause);
        else
            ibPlay.setImageResource(R.drawable.mediacontorller_play);
        initPlayModeIcon(false);
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying())
            mPlayer.pause();
        else
            mPlayer.start();
        updatePausePlay();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (ibPlay != null)
            ibPlay.setEnabled(enabled);
        if (mProgress != null)
            mProgress.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public interface OnShownListener {
        void onShown();
    }

    public interface OnHiddenListener {
        void onHidden();
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        long getDuration();

        long getCurrentPosition();

        void seekTo(long pos);

        boolean isPlaying();

        int getBufferPercentage();

    }

    /**
     * 定义activity需要监听ui的接口
     */
    private MediaPlayerUIListener mMediaPlayerUIListener;

    public interface MediaPlayerUIListener {
        void onBack();

        void onPrev();

        void onNext();

        void onSwitchTrack();
    }

    public void setMediaPlayerUIListener(MediaPlayerUIListener listener) {

        mMediaPlayerUIListener = listener;
    }

    /**
     * 接收从videoPlayActivity传过来的数据
     */
    public final String MICROPHONE_ACTION = "com.efercro.action.MICROPHONE_ACTION";

    class MyBroadcaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(MICROPHONE_ACTION, action)) {
                if (intent.getIntExtra("state", 0) == 1) {//拔出

                } else if (intent.getIntExtra("state", 0) == 0) {// 插入
                }
            }

        }
    }

    //注册广播
    public void registerReceiver(Context context) {
        myBroadcaseReceiver = new MyBroadcaseReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MICROPHONE_ACTION);
        if (context != null) {
            context.registerReceiver(myBroadcaseReceiver, intentFilter);
        }
    }

    //注销广播
    public void unregisterReceiver(Context context) {
        if (myBroadcaseReceiver != null && context != null) {
            context.unregisterReceiver(myBroadcaseReceiver);
        }
    }


    /**
     * {@link #initListener()}
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ib_back_vitamio) {
            if (mMediaPlayerUIListener != null) {
                mMediaPlayerUIListener.onBack();
            }

        } else if (i == R.id.ib_playmode_vitamio) {
            initPlayModeIcon(true);

        } else if (i == R.id.ib_prev_vitamio) {
            if (mMediaPlayerUIListener != null) {
                mMediaPlayerUIListener.onPrev();
            }

        } else if (i == R.id.ib_next_vitamio) {
            if (mMediaPlayerUIListener != null) {
                mMediaPlayerUIListener.onNext();
            }

        } else if (i == R.id.img_accompany) {
            com.booyue.karaoke.utils.LoggerUtils.d(TAG + "imag_accompany");
            switchTrack();

        } else if (i == R.id.img_original) {
            com.booyue.karaoke.utils.LoggerUtils.d(TAG + "imag_ori");
            switchTrack();

        } else if (i == R.id.img_media_volume) {//背景音量调节
            setVolume();
        } else if (i == R.id.fl_video_vitamio) {//点击屏幕弹出window或者销毁window
            if (isShowing()) {
                hide();
            } else {
                show(sDefaultTimeout);
            }
        }
    }

    /**
     * 声轨切换（伴奏，原唱）
     */
    private static final String TAG = "MediaController-----";
    private List<MediaPlayer.TrackInfo> audioTrackInfos = new ArrayList<>();

    //判断是否支持音轨切换
    public boolean isSupportAudioTrackTransaction() {
        if (mPlayer != null) {
            audioTrackInfos.clear();
            MediaPlayer.TrackInfo[] trackInfos = mPlayer.getTrackInfo();
            if (trackInfos != null && trackInfos.length > 0) {
                for (int i = 0; i < trackInfos.length; i++) {
                    if (trackInfos[i].getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                        audioTrackInfos.add(trackInfos[i]);
                    }
                }
                if (audioTrackInfos.size() >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final String micPath = "/sys/class/switch/micdet/state";

    ///检测mic是否已经插入
    private String checkMic() {
        android.util.Log.d(TAG, "checkMic: ");
        try {
            File micFile = new File(micPath);
            if (micFile == null || !micFile.exists()) return null;
//            FileInputStream fileInputStream = new FileInputStream(micFile);
            BufferedReader bfr = new BufferedReader(new FileReader(micFile));
            String line = bfr.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = bfr.readLine();
            }
            bfr.close();
            Log.d("buffer", "bufferRead: " + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //音轨切换
    private long preClickTime = 0;

    public void switchTrack() {
        //控制点击的频繁度
        if (System.currentTimeMillis() - preClickTime < 2000) {
            preClickTime = System.currentTimeMillis();
            com.booyue.karaoke.utils.LoggerUtils.d(TAG + "click_delta = " + (System.currentTimeMillis() - preClickTime));
            return;
        }

        if (isAccompany) {
            mPlayer.selectTrack(1);
            initAccompanyView(false);
            isAccompany = false;
        } else {
            mPlayer.selectTrack(2);
            initAccompanyView(true);
            isAccompany = true;
        }
    }

    /**
     * 资源释放
     */
    public void release() {
        if (isShowing()) {
            hide();
        }
        if (mMediaPlayerUIListener != null) {
            mMediaPlayerUIListener = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mAM != null) {
            mAM = null;
        }
        if (mAnchor != null) {
            mAnchor = null;
        }
        if (mPlayer != null) {
            mPlayer = null;
        }

    }


    /**
     * 视频播放模式
     */
    public static final int STATE_PLAY_MODE_SINGLE = 1;
    public static final int STATE_PLAY_MODE_LIST = 2;
    public static int STATE_PLAY_MODE = STATE_PLAY_MODE_LIST;


    public void setVolume() {

        AudioManager audioMa = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioMa.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND
                        | AudioManager.FLAG_SHOW_UI);
    }


    /**
     * 是否是卡拉kalaoke
     */

    private boolean isAccompany = false;

    public void setSupportKalaoke() {
        if (isSupportAudioTrackTransaction()) {
            //无法调节干脆去掉
//            String stateStr =  checkMic().trim();
//            char c = stateStr.charAt(0);
//            int state = c;
//            android.util.Log.d(TAG, "setSupportKalaoke: state = " + state);
//            if(state == 48){
//                ivMicrophone.setVisibility(GONE);
//            }else {
//                ivMicrophone.setVisibility(VISIBLE);
//            }
            //原唱
            if (!isAccompany) {
                com.booyue.karaoke.utils.LoggerUtils.d(TAG + "isAccompnay =  false");
                initAccompanyView(false);
                //伴唱
            } else {
                mPlayer.selectTrack(2);
                com.booyue.karaoke.utils.LoggerUtils.d(TAG + "isAccompnay =  true");
                isAccompany = true;
                initAccompanyView(true);
            }
        } else {
            //不支持音轨切换，隐藏功能键
            if (ivAccompany.getVisibility() != GONE) {
                ivAccompany.setVisibility(GONE);
            }
            if (ivOriginal != null && ivOriginal.getVisibility() != GONE) {
                ivOriginal.setVisibility(GONE);
            }
        }
    }

    /**
     * 初始化原唱伴唱视图
     *
     * @param isAccompany true 伴唱 false 原唱
     */
    private void initAccompanyView(boolean isAccompany) {
        if (!isAccompany) {
            if (ivOriginal != null && ivOriginal.getVisibility() == GONE) {
                ivOriginal.setVisibility(VISIBLE);
            }
            if (ivAccompany != null && ivAccompany.getVisibility() == VISIBLE) {
                ivAccompany.setVisibility(GONE);
            }
        } else {
            if (ivOriginal != null && ivOriginal.getVisibility() == VISIBLE) {
                ivOriginal.setVisibility(GONE);
            }
            if (ivAccompany != null && ivAccompany.getVisibility() == GONE) {
                ivAccompany.setVisibility(VISIBLE);
            }
        }
    }
}
