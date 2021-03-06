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

package io.vov.vitamio.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.lang.reflect.Method;

import io.vov.vitamio.MediaFormat;
import io.vov.vitamio.R;
import io.vov.vitamio.handler.AudioPlayHandler;
import io.vov.vitamio.handler.AudioRecordHandler;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.utils.LoggerUtils;
import io.vov.vitamio.utils.StringUtils;

/**
 * A view containing controls for a MediaPlayer. Typically contains the buttons
 * like "Play/Pause" and a progress slider. It takes care of synchronizing the
 * controls with the state of the MediaPlayer.
 * <p/>
 * The way to use this class is to a) instantiate it programatically or b)
 * create it in your xml layout.
 * <p/>
 * a) The MediaController will create a default set of controls and put them in
 * a window floating above your application. Specifically, the controls will
 * float above the view specified with setAnchorView(). By default, the window
 * will disappear if left idle for three seconds and reappear when the user
 * touches the anchor view. To customize the MediaController's style, layout and
 * controls you should extend MediaController and override the {#link
 * {@link #makeControllerView()} method.
 * <p/>
 * b) The MediaController is a FrameLayout, you can put it in your layout xml
 * and get it through {@link #findViewById(int)}.
 * <p/>
 * NOTES: In each way, if you want customize the MediaController, the SeekBar's
 * id must be mediacontroller_progress, the Play/Pause's must be
 * mediacontroller_pause, current time's must be mediacontroller_time_current,
 * total time's must be mediacontroller_time_total, file name's must be
 * mediacontroller_file_name. And your resources must have a pause_button
 * drawable and a play_button drawable.
 * <p/>
 * Functions like show() and hide() have no effect when MediaController is
 * created in an xml layout.
 */
public class MediaController extends FrameLayout implements View.OnClickListener {
    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int SWITCH_AUDIOTRACK = 3;


    private MediaPlayerControl mPlayer;
    private Context mContext;
    private PopupWindow mWindow;
    private int mAnimStyle;
    private View mAnchor;
    private View mRoot;
    //  private SeekBar mProgress;
//  private TextView mEndTime, mCurrentTime;
//  private TextView mFileName;
    private OutlineTextView mInfoView;
    private String mTitle;
    private long mDuration;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mInstantSeeking = false;
    private boolean mFromXml = false;
    //  private ImageButton mPauseButton;
    private AudioManager mAM;
    private OnShownListener mShownListener;
    private OnHiddenListener mHiddenListener;

    private ImageView ibPlay;
    private ImageView ibPlayMode;
    private ImageView ibPrev;
    private ImageView ibNext;

    private ImageButton ibBack;
    private ImageView ivAccompany;
    private ImageView ivMicrophone;
    private ImageView ivMediaVolume;
    private FrameLayout flVideo;

    private SeekBar mProgress;
    private TextView tvDuration, tvCurrentime;
    private TextView tvFileName;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
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
    private View.OnClickListener mPauseListener = new View.OnClickListener() {
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
                mPlayer.seekTo(newposition);
            if (mInfoView != null)
                mInfoView.setText(time);
            if (tvCurrentime != null)
                tvCurrentime.setText(time);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (!mInstantSeeking)
                mPlayer.seekTo((mDuration * bar.getProgress()) / 1000);
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
    private View volumeView;
    private SeekBar volume_progress;
    private TextView tvProgress;
    private LinearLayout accompanyLayout;
    private MyBroadcaseReceiver myBroadcaseReceiver;


    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mFromXml = true;
        initController(context);
    }

    public MediaController(Context context) {
        super(context);
        if (!mFromXml && initController(context))
            initFloatingWindow();
        //用于接收信息的广播
//    mReceiver = new MyBroadcaseReceiver();
    }
//  private MyBroadcaseReceiver mReceiver;

//  public MyBroadcaseReceiver getReceiver() {
//    return mReceiver;
//  }

    public void initPlayModeIcon(boolean isSwitch) {
        if (isSwitch) {
            if (STATE_PLAY_MODE == STATE_PLAY_MODE_SINGLE) {
                ibPlayMode.setImageResource(R.drawable.mediacontorller_cycle);
                STATE_PLAY_MODE = STATE_PLAY_MODE_LIST;

            } else if (STATE_PLAY_MODE == STATE_PLAY_MODE_LIST) {
                ibPlayMode.setImageResource(R.drawable.mediacontorller_cycle);
                STATE_PLAY_MODE = STATE_PLAY_MODE_SINGLE;
            }
        } else {
            if (STATE_PLAY_MODE == STATE_PLAY_MODE_SINGLE) {
                ibPlayMode.setImageResource(R.drawable.mediacontorller_cycle);
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

    /**
     * Set the view that acts as the anchor for the control view. This can for
     * example be a VideoView, or your Activity's main view.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(View view) {
        mAnchor = view;
        if (!mFromXml) {
            removeAllViews();
            mRoot = makeControllerView();
            mWindow.setContentView(mRoot);
            mWindow.setWidth(LayoutParams.MATCH_PARENT);
            mWindow.setHeight(LayoutParams.MATCH_PARENT);
        }
        initControllerView(mRoot);
    }

    /**
     * Create the view that holds the widgets that control playback. Derived
     * classes can override this to create their own.
     *
     * @return The controller view.
     */
    protected View makeControllerView() {
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(getResources().getIdentifier("mediacontroller_karaoke", "layout", mContext.getPackageName()), this);
    }

    private void initControllerView(View v) {
//    mPauseButton = (ImageButton) v.findViewById(getResources().getIdentifier("mediacontroller_play_pause", "id", mContext.getPackageName()));
//        ibBack = getViewById(v, R.id.ib_back_vitamio);
        volumeView = getViewById(v, R.id.layout_volume);
        initVolumeView(volumeView);
        volumeView.setVisibility(GONE);

        ibBack = getViewById(v, R.id.ib_back_vitamio);
        tvFileName = getViewById(v, R.id.tv_name_vitamio);

        tvCurrentime = getViewById(v, R.id.tv_currenttime_vitamio);
        tvDuration = getViewById(v, R.id.tv_durationtime_vitamio);

        ibPlayMode = getViewById(v, R.id.ib_playmode_vitamio);
        ibPrev = getViewById(v, R.id.ib_prev_vitamio);
        ibPlay = getViewById(v, R.id.ib_play_vitamio);
        ibNext = getViewById(v, R.id.ib_next_vitamio);
        ivMediaVolume = getViewById(v, R.id.img_media_volume);

        accompanyLayout = getViewById(v, R.id.fl_accompany);
        ivAccompany = getViewById(v, R.id.img_accompany);
        ivMicrophone = getViewById(v, R.id.img_microphone);
        ivMicrophone.setVisibility(GONE);
        flVideo = getViewById(v, R.id.fl_video_vitamio);
        initListener();
        if (ibPlay != null) {
            ibPlay.requestFocus();
            ibPlay.setOnClickListener(mPauseListener);
        }

        mProgress = (SeekBar) v.findViewById(R.id.sb_progress_vitamio);
        if (mProgress != null) {
            mProgress.setOnSeekBarChangeListener(mSeekListener);
//      mProgress.setThumbOffset(1);
            mProgress.setMax(1000);
        }
    }

    /**
     * {@link #onClick(View)}
     */
    private void initListener() {
        ibBack.setOnClickListener(this);
        ibPlayMode.setOnClickListener(this);
        ibPrev.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ivAccompany.setOnClickListener(this);
//        ivMicrophone.setOnClickListener(this);
        flVideo.setOnClickListener(this);
        ivMediaVolume.setOnClickListener(this);
//        volumeView.setOnClickListener(this);
    }

    public void setMediaPlayer(MediaPlayerControl player) {
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
        mTitle = name;
        if (tvFileName != null)
            tvFileName.setText(mTitle);
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

//  private void disableUnsupportedButtons() {
//    try {
//      if (ibPlay != null && !mPlayer.canPause())
//        ibPlay.setEnabled(false);
//    } catch (IncompatibleClassChangeError ex) {
//    }
//  }

    /**
     * <p>
     * Change the animation style resource for this controller.
     * </p>
     * <p/>
     * <p>
     * If the controller is showing, calling this method will take effect only the
     * next time the controller is shown.
     * </p>
     *
     * @param animationStyle animation style to use when the controller appears
     *                       and disappears. Set to -1 for the default animation, 0 for no animation, or
     *                       a resource identifier for an explicit animation.
     */
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
            if (isSupportKalaoke && accompanyLayout != null && accompanyLayout.getVisibility() == GONE) {
                accompanyLayout.setVisibility(VISIBLE);
            } else if (!isSupportKalaoke && accompanyLayout != null && accompanyLayout.getVisibility() == VISIBLE) {
                accompanyLayout.setVisibility(GONE);
            }
//      disableUnsupportedButtons();

            if (mFromXml) {
                setVisibility(View.VISIBLE);
            } else {
                int[] location = new int[2];

                mAnchor.getLocationOnScreen(location);
                Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1] + mAnchor.getHeight());

                mWindow.setAnimationStyle(mAnimStyle);
                setWindowLayoutType();
                mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, anchorRect.left, anchorRect.bottom);
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
        if (volumeView.getVisibility() == VISIBLE) {
            volumeView.setVisibility(GONE);
        }

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
            int percent = mPlayer.getBufferPercentage();
            // TODO: 2017/9/14
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
            ibPlay.setImageResource(R.drawable.selector_pause);
//      mPauseButton.setImageResource(getResources().getIdentifier("mediacontroller_pause", "drawable", mContext.getPackageName()));
        else
            ibPlay.setImageResource(R.drawable.selector_play);
//      mPauseButton.setImageResource(getResources().getIdentifier("mediacontroller_play", "drawable", mContext.getPackageName()));
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
//    disableUnsupportedButtons();

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

//    boolean canPause();
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
            if(TextUtils.equals(MICROPHONE_ACTION,action)){
                if (intent.getIntExtra("state", 0) == 0) {//拔出
//                    if(ivMicrophone != null){
//                        ivMicrophone.setVisibility(GONE);
//                    }

                } else if (intent.getIntExtra("state", 0) == 1) {// 插入
//                    if(ivMicrophone != null){
//                        ivMicrophone.setVisibility(VISIBLE);
//                    }
                }
            }

        }
    }
    //注册广播
    public void registerReceiver(Context context){
        myBroadcaseReceiver = new MyBroadcaseReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MICROPHONE_ACTION);
        if(context != null){
            context.registerReceiver(myBroadcaseReceiver,intentFilter);
        }
    }
    //注销广播
    public void unregisterReceiver(Context context){
        if(myBroadcaseReceiver != null && context != null){
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
            if((System.currentTimeMillis() - previousClickTimes) < 1000 * 3){
                if (isFirstToast) {
//                    Toast.makeText(mContext, "正在切换...", Toast.LENGTH_SHORT).show();
                    isFirstToast = false;
                }
                return;
            }
            isFirstToast = true;
            previousClickTimes = System.currentTimeMillis();
            switchTrack();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    boolean isAccompany = switchTrack();
//                    Message message = new Message();
//                    message.what = SWITCH_AUDIOTRACK;
//                    message.obj = isAccompany;
//                    mHandler.sendMessage(message);
//                }
//            }).start();
//            if (mMediaPlayerUIListener != null) {
//                mMediaPlayerUIListener.onSwitchTrack();
//            }

        }else if (i == R.id.img_media_volume) {//背景音量调节
//            if (volumeView.getVisibility() == GONE) {
//                volumeView.setVisibility(VISIBLE);
//            } else {
//                volumeView.setVisibility(GONE);
//            }
            setVolume();

        } else if (i == R.id.fl_video_vitamio) {//点击屏幕弹出window或者销毁window
            if (isShowing()) {
                hide();
            } else {
                show(sDefaultTimeout);
            }
        }
    }


    public void setVolume(){
        final AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //最大音量
        final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前音量
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        int currentVolumeProgress = (int) ((float) currentVolume * 100 / maxVolume);
        currentVolume = currentVolume + 1;
        if(currentVolume > maxVolume){
            currentVolume = currentVolume - 2;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FX_KEY_CLICK);
    }

//    private boolean isAccompany = false;
    private long previousClickTimes;
    private boolean isFirstToast = true;//在规定的时间内，如果第一次就弹吐司，否则不弹

    /**
     * 声轨切换（伴奏，原唱）
     */
    private static final String TAG = "MediaController-----";

    public boolean switchTrack() {
        if (mAnchor != null && (mAnchor instanceof LinearLayout)) {
            LinearLayout linearLayout = (LinearLayout) mAnchor;
            VideoView videoView = (VideoView) linearLayout.getChildAt(0);
            SparseArray<MediaFormat> audioTrackMap = videoView.getAudioTrackMap("utf-8");
            LoggerUtils.d(TAG + "audioTrackMap : " + audioTrackMap.size());
            if (audioTrackMap.size() <= 0) {
                return false;
            }
            if (audioTrackMap.size() == 2) {
                int accompanyKey = audioTrackMap.keyAt(1);//2伴奏 1 取消伴奏
                int originalKey = audioTrackMap.keyAt(0);//2伴奏 1 取消伴奏
//                MediaFormat originalValue = audioTrackMap.valueAt(0);
//                MediaFormat accompanyValue = audioTrackMap.valueAt(1);
                int currentTrack = videoView.getAudioTrack();
//                LoggerUtils.d(TAG + "accompanyKey: " + accompanyKey + ",originalKey = " + originalKey + ",currentTrack = " + currentTrack);
//                LoggerUtils.d(TAG + "originalValue = " + originalValue.toString() + ",accompanyValue = " + accompanyValue.toString());
                if(accompanyKey == currentTrack){
                    videoView.setAudioTrack(originalKey);
                    if(ivAccompany != null){
                        ivAccompany.setImageResource(R.drawable.shakelight);
                    }
                    isAccompany = false;
                    return false;
                }else if(originalKey == currentTrack){
                    videoView.setAudioTrack(accompanyKey);
                    if(ivAccompany != null){
                        ivAccompany.setImageResource(R.drawable.accompany);
                    }
                    isAccompany = true;
                    return true;
                }
            }
            return false;
        }
        return false;
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


    private void initVolumeView(View volumeView) {
        volume_progress = (SeekBar) volumeView.findViewById(R.id.seekbar_volume);
        tvProgress = (TextView) volumeView.findViewById(R.id.tv_progress);
        final AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //最大音量
        final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前音量
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int currentVolumeProgress = (int) ((float) currentVolume * 100 / maxVolume);
        tvProgress.setText(currentVolumeProgress + "/100");
        volume_progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvProgress.setText(progress + "/100");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(FADE_OUT);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress * maxVolume / 100, AudioManager.FX_KEY_CLICK);
                show();
            }
        });
    }

    /**
     * 是否是卡拉kalaoke
     */

    private boolean isSupportKalaoke;
    private boolean isAccompany = false;
    public void setSupportKalaoke() {
        if (mAnchor == null || !(mAnchor instanceof LinearLayout)) return;
        LinearLayout linearLayout = (LinearLayout) mAnchor;
        VideoView videoView = (VideoView) linearLayout.getChildAt(0);
        SparseArray<MediaFormat> audioTrackMap = videoView.getAudioTrackMap("utf-8");
        android.util.Log.d(TAG, "setSupportKalaoke audioTrackMap : " + audioTrackMap.size());
        int accompanyKey = audioTrackMap.keyAt(1);//2伴奏 1 取消伴奏
        int originalKey = audioTrackMap.keyAt(0);//2伴奏 1 取消伴奏
//                MediaFormat originalValue = audioTrackMap.valueAt(0);
//                MediaFormat accompanyValue = audioTrackMap.valueAt(1);
        int currentTrack = videoView.getAudioTrack();
        if (audioTrackMap.size() > 1) {
            this.isSupportKalaoke = true;
        } else {
            this.isSupportKalaoke = false;
        }
        if (isSupportKalaoke) {
//            initializeAudio();//声音的输入，K歌的效果
            if(currentTrack != accompanyKey && isAccompany){
                switchTrack();
            }else if(currentTrack != originalKey && !isAccompany){
                switchTrack();
            }else if(currentTrack == accompanyKey){
                if(ivAccompany != null){
                    ivAccompany.setImageResource(R.drawable.accompany);
                }

                isAccompany = true;
            }else if(currentTrack == originalKey){
                if(ivAccompany != null){
                    ivAccompany.setImageResource(R.drawable.shakelight);
                }

                isAccompany = false;
            }
        }
    }

    /**
     * 录音初始化
     */
    private void initializeAudio() {
        AudioRecordHandler audioRecordHandler = new AudioRecordHandler();
        final AudioPlayHandler audioPlayHandler = new AudioPlayHandler();
        audioRecordHandler.startRecord(new AudioRecordHandler.AudioRecordingCallback() {
            @Override
            public void onRecording(byte[] data, int startIndex, int length) {
                audioPlayHandler.prepare();
                audioPlayHandler.onRecordDataCallback(data,startIndex,length);
            }
            @Override
            public void onStopRecord(String savedPath) {
            }
        });
//        recorder = new MediaRecorder();
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        recorder.setOutputFile("/sdcard/peipei.amr");
//        try {
//            recorder.prepare();
//            recorder.start();
//            return;
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//            return;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
