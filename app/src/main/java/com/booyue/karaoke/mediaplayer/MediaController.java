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

package com.booyue.karaoke.mediaplayer;

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


    private MediaPlayer mPlayer;
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

    private FrameLayout ibBack;
    private FrameLayout ivAccompany;
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
    private View volumeView;
    private SeekBar volume_progress;
    private TextView tvProgress;
    private LinearLayout accompanyLayout;
    private MyBroadcaseReceiver myBroadcaseReceiver;
    private ImageView ivOriginal;


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
//        initVolumeView(volumeView);
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
        ivOriginal = getViewById(v, R.id.img_original);
        ivOriginal.setVisibility(GONE);
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
        ivOriginal.setOnClickListener(this);
        flVideo.setOnClickListener(this);
        ivMediaVolume.setOnClickListener(this);
//        volumeView.setOnClickListener(this);
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
     * @param isHHTMedia 判断当前播放的文件是否是内置文件（做特俗处理）
     */
    public void setFileName(String name, boolean isHHTMedia) {

//        //如果是系统内置的文件
//        if (name.startsWith(HHT_MEDIA_ROOTPATH)) {
//            mTitle = swapTitle(name);
//        } else {
//            //不是系统内置的文件
//            mTitle = name;
//        }
        //去掉前面用于排序的字符
        if (isHHTMedia) {
            name = name.substring(2, name.length());
            mTitle = swapTitle(name);
        } else {
            mTitle = name;
        }


        if (tvFileName != null)
            tvFileName.setText(mTitle);
    }


    /**
     * 这个地方纯属瞎弄
     *
     * @param mTitle
     * @return
     */
    private String swapTitle(String mTitle) {
        //-----------------------多纳英语----------------------------start
//        public static final String HHT_XT_ZJYY_DUONA_01 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "zaoshanghao.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_02 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "woshi.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_03 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "meiweishuiguo.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_04 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "rangwomenshuyishu.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_05 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "shitoujiandaobu.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_06 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "zhuomicang.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_07 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "chuanshangyifu.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_08 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "shangrikuaile.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_09 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "wotule.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_10 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "moxojiamshenmeyanse.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_11 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "qudongwuyuan.mp4";
//        public static final String HHT_XT_ZJYY_DUONA_12 = HHT_XT_ZJYY_DUONA_ROOT_PATH + "keaidedongwu.mp4";
        //-----------------------多纳英语----------------------------end
//
//        //火火兔乐园---- 卡拉ok ---经典儿歌
//        public static final String HHT_LY_KALAOK_JDEG_ROOT_PATH = "/system/videos/leyuan/kalaOK/jingdian/";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_01 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "fenshuajiang03.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_02 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "yifenqian09.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_03 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "woniuyuhuangliniao07.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_04 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "lingerxiangdingdang04.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_05 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "maliyouzhixiaoyanggao19.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_06 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "liangzhilaohu10.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_07 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "shishangzhiyoumamahao05.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_08 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "shuyazi06.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_09 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "aiwojiubaobaowo18.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_10 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "baluobo15.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_11 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "chuntianzainali01.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_12 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "lanjinling02.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_13 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "shijianzaishuohua17.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_14 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "woyouyigejia13.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_15 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "xiaolongren12.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_16 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "xiaoxingxing08.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_17 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "xiaoyanzi11.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_18 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "zhaopengyou14.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_19 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "zhongtaiyang20.mkv";
//        public static final String HHT_LY_KALAOK_JDEG_ITEM_20 = HHT_LY_KALAOK_JDEG_ROOT_PATH + "zhuoniqiu16.mkv";
//
//        //火火兔乐园---- 卡拉ok ---原创儿歌
//        public static final String HHT_LY_KALAOK_TTMTV_ROOT_PATH = "/system/videos/leyuan/kalaOK/yuanchuang/";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_01 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "daxiongmao01.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_02 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "chunyu02.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_03 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "chaojiyingxiong03.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_04 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "zhuqingting04.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_05 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "shangyuanzhuzhici05.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_06 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "shuyazi06.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_07 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "yinghuoweiguang07.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_08 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "bahe08.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_09 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "hello09.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_10 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "gongfuxiaozi10.mp4";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_11 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "xunyinzhibuyu11.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_12 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "xiaoxiaobinggan12.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_13 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "xiaohuajia13.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_14 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "shancunyonghuai14.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_15 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "gongxifacai15.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_16 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "mingnong16.mp4";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_17 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "jinglige17.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_18 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "chuntianzainali18.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_19 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "zhishuge19.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_20 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "aixiliangshi20.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_21 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "hua21.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_22 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "qiuyinong22.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_23 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "meilitianye23.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_24 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "laoshukaihui24.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_25 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "wuyixiang25.mkv";
//        public static final String HHT_LY_KALAOK_TTMTV_ITEM_26 = HHT_LY_KALAOK_TTMTV_ROOT_PATH + "daguonian26.mkv";
//
//        //火火兔乐园---- 卡拉ok ----火火兔唱古诗
//        public static final String HHT_LY_KALAOK_HHTCGS_ROOT_PATH = "/system/videos/leyuan/kalaOK/hhtcgs/";
//        public static final String HHT_LY_KALAOK_HHTCGS_01 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "ye01.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_02 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "jys02.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_03 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "yr03.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_04 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "lzc04.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_05 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "cs05.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_06 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "bdd06.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_07 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "glyx07.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_08 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "fdgyclb08.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_09 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "xyzby09.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_10 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "xc10.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_11 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "sx11.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_12 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "jhzs12.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_13 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "zfbdc13.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_14 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "zccsb14.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_15 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "cyxy15.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_16 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "cx16.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_17 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "wlspb17.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_18 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "fqyb18.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_19 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "jpdbxh19.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_20 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "jx20.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_21 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "yzy21.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_22 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "dgql22.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_23 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "jj23.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_24 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "cgx24.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_25 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "cgx24.mp4";
//        public static final String HHT_LY_KALAOK_HHTCGS_26 = HHT_LY_KALAOK_HHTCGS_ROOT_PATH + "cgx24.mp4";

// 火火兔学堂---早教英语----邦尼英语
//        public static final String HHT_XT_ZJYY_BANGNI_01 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a0Hello_how_are_you" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_02 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a1Where_is_Bunny" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_03 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a2Boy_and_girl" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_04 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a3Stand_up_sit_down" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_05 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a4Wash_your_hands" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_06 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a5Nice_food" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_07 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a6I_see" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_08 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a7Whats_this" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_09 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a8A_candy_for_you" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_10 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "a9One_potato" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_11 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b0Happy" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_12 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b1Shake" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_13 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b2Which_one_do_you_like" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_14 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b3Who_can_do_it" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_15 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b4I_want" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_16 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b5Traffic_lights" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_17 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b6How_do_you_feel" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_18 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b7Mid_Autumn_Day" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_19 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b8Taste" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_20 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "b9What_are_you_wearing" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_21 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c0I_see_you" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_22 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c1Music_man" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_23 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c2Rain" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_24 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c3Merry_Christmas" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_25 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c4What_shape_is_it" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_26 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c5Numbers" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_27 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c6Fly_a_kite" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_28 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c7Is_it_yours" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_29 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c8Three_bears" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_30 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "c9At_home" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_31 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d0Its_too_hot" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_32 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d1Who_is_coming" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_33 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d2A_busy_day" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_34 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d3Little_rabbits" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_35 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d4Can_i_help_you" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_36 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d5Three_little_pigs" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_37 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d6I_am_a_bus" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_38 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d7Bingo" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_39 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d8Five_little_monkeys" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_40 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "d9Chinese_new_year" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_41 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "e0Fun_to_play" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_42 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "e1Rabbit_and_tortoise" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_43 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "e2What_is_your_favorite_sport" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_44 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "e3Bears_teeth" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_45 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "e4Happy_mothers_day" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_46 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "e5A_big_radish_in_the_hole" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_47 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "e6The_muffin_man" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BANGNI_48 = HHT_XT_ZJYY_BANGNI_ROOT_PATH + "e7Goodbye" + MediaFromart.MP4;

// 火火兔学堂---早教英语----宝狄英语
//        public static final String HHT_XT_ZJYY_BAODI_01 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a0Bodhis_ABC" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_02 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a1A_Day_at_Cafe_Sambolo_1" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_03 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a2All_These_Shapes1" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_04 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a3America_Song" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_05 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a4Around_My_Rooms" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_06 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a5Asia_Song" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_07 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a6Better_Weather" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_08 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a7Body_Bop" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_09 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a8Check_out_my_toy" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_10 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "a9City_Life_City_Lights" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_11 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b0Clean_and_Green" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_12 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b1Colourful_Party" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_13 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b2Count_on_You" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_14 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b3Count_with_Bodhi" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_15 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b4Day_and_Night" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_16 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b5Dream_Team" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_17 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b6Dress_You_Up" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_18 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b7Everyday_Heroes" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_19 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b8Face_It" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_20 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "b9Family" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_21 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c0Food" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_22 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c1Friends_Forever" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_23 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c2Full_Circle" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_24 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c3Fun_Family_Day" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_25 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c4Future_of_the_World" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_26 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c5Good_Food_Good_for_You" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_27 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c6I_Can_Do_It" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_28 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c7I_Love_Food" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_29 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c8Imagine" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_30 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "c9In_Our_Neighbourhood" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_31 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d0Its_a_New_Day" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_32 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d1Lets_Get_Fit" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_33 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d2Lets_Go_to_School" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_34 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d3Lets_Have_a_Good_Time" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_35 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d4Lets_Take_It_from_123" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_36 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d5Lets_play_ball" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_37 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d6Make_This_the_Best_Day" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_38 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d7Meet_Our_Families" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_39 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d8Mix_and_Match" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_40 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "d9Move_Our_Bodies" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_41 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e0My_Heart" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_42 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e1My_Home" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_43 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e2No_Place_Like_Home" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_44 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e3Pat_Your_Pet" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_45 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e4Pet_Sounds" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_46 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e5Push_Through_Dont_Stop" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_47 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e6Rainbow_Shining" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_48 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e7Reap_What_You_Sow" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_49 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e8School_Days" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_50 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "e9See_you_later" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_51 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f0Sharing_is_Caring" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_52 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f1Sing_Along_with_Bodhi_and_Friends" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_53 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f2So_Much_to_Do" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_54 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f3Thats_What_We_Like" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_55 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f4The_Face_Song" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_56 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f5The_Greetings_Song" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_57 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f6The_Hello_Song" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_58 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f7The_Name_of_the_Game" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_59 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f8The_Sounds_of_the_Farm" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_60 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "f9The_time_is_now" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_61 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g0Things_That_Go" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_62 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g1Together" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_63 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g2Toys_toys_everywhere" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_64 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g3We_Are_on_the_Go" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_65 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g4We_Can_Be_Anything" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_66 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g5We_ll_Be_Your_Shelter" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_67 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g6Weather_Ready" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_68 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g7Welcome_to_Our_World" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_69 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g8Welcome_to_the_Farm" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_70 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "g9When_I_Grow_Up" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_71 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "h0With_My_Eyes" + MediaFromart.MP4;
//        public static final String HHT_XT_ZJYY_BAODI_72 = HHT_XT_ZJYY_BAODI_ROOT_PATH + "h1Work_Hard_Get_Smart" + MediaFromart.MP4;


        switch (mTitle) {
            //-----------------------邦尼英语----------------------------start
            case "Hello_how_are_you":
                mTitle = "01.Hello_how_are_you";
                break;
            case "Where_is_Bunny":
                mTitle = "02.Where_is_Bunny";
                break;
            case "Boy_and_girl":
                mTitle = "03.Boy_and_girl";
                break;
            case "Stand_up_sit_down":
                mTitle = "04.Stand_up_sit_down";
                break;
            case "Wash_your_hands":
                mTitle = "05.Wash_your_hands";
                break;
            case "Nice_food":
                mTitle = "06.Nice_food";
                break;
            case "I_see":
                mTitle = "07.I_see";
                break;
            case "Whats_this":
                mTitle = "08.Whats_this";
                break;
            case "A_candy_for_you":
                mTitle = "09.A_candy_for_you";
                break;
            case "One_potato":
                mTitle = "10.One_potato";
                break;
            case "Happy":
                mTitle = "11.Happy";
                break;
            case "Shake":
                mTitle = "12.Shake";
                break;
            case "Which_one_do_you_like":
                mTitle = "13.Which_one_do_you_like";
                break;
            case "Who_can_do_it":
                mTitle = "14.Who_can_do_it";
                break;
            case "I_want":
                mTitle = "15.I_want";
                break;
            case "Traffic_lights":
                mTitle = "16.Traffic_lights";
                break;
            case "How_do_you_feel":
                mTitle = "17.How_do_you_feel";
                break;
            case "Mid_Autumn_Day":
                mTitle = "18.Mid_Autumn_Day";
                break;
            case "Taste":
                mTitle = "19.Taste";
                break;
            case "What_are_you_wearing":
                mTitle = "20.What_are_you_wearing";
                break;
            case "I_see_you":
                mTitle = "21.I_see_you";
                break;
            case "Music_man":
                mTitle = "22.Music_man";
                break;
            case "Rain":
                mTitle = "23.Rain";
                break;
            case "Merry_Christmas":
                mTitle = "24.Merry_Christmas";
                break;
            case "What_shape_is_it":
                mTitle = "25.What_shape_is_it";
                break;
            case "Numbers":
                mTitle = "26.Numbers";
                break;
            case "Fly_a_kite":
                mTitle = "27.Fly_a_kite";
                break;
            case "Is_it_yours":
                mTitle = "28.Is_it_yours";
                break;
            case "Three_bears":
                mTitle = "29.Three_bears";
                break;
            case "At_home":
                mTitle = "30.At_home";
                break;
            case "Its_too_hot":
                mTitle = "31.Its_too_hot";
                break;
            case "Who_is_coming":
                mTitle = "32.Who_is_coming";
                break;
            case "A_busy_day":
                mTitle = "33.A_busy_day";
                break;
            case "Little_rabbits":
                mTitle = "34.Little_rabbits";
                break;
            case "Can_i_help_you":
                mTitle = "35.Can_i_help_you";
                break;
            case "Three_little_pigs":
                mTitle = "36.Three_little_pigs";
                break;
            case "I_am_a_bus":
                mTitle = "37.I_am_a_bus";
                break;
            case "Bingo":
                mTitle = "38.Bingo";
                break;
            case "Five_little_monkeys":
                mTitle = "39.Five_little_monkeys";
                break;
            case "Chinese_new_year":
                mTitle = "40.Chinese_new_year";
                break;
            case "Fun_to_play":
                mTitle = "41.Fun_to_play";
                break;
            case "Rabbit_and_tortoise":
                mTitle = "42.Rabbit_and_tortoise";
                break;
            case "What_is_your_favorite_sport":
                mTitle = "43.What_is_your_favorite_sport";
                break;
            case "Bears_teeth":
                mTitle = "44.Bears_teeth";
                break;
            case "Happy_mothers_day":
                mTitle = "45.Happy_mothers_day";
                break;
            case "A_big_radish_in_the_hole":
                mTitle = "46.A_big_radish_in_the_hole";
                break;
            case "The_muffin_man":
                mTitle = "47.The_muffin_man";
                break;
            case "Goodbye":
                mTitle = "48.Goodbye";
                break;
            //-----------------------邦尼英语----------------------------end

            //-----------------------宝狄英语----------------------------start
            case "Bodhis_ABC":
                mTitle = "01.Bodhis_ABC";
                break;
            case "A_Day_at_Cafe_Sambolo_1":
                mTitle = "02.A_Day_at_Cafe_Sambolo_1";
                break;
            case "All_These_Shapes1":
                mTitle = "03.All_These_Shapes1";
                break;
            case "America_Song":
                mTitle = "04.America_Song";
                break;
            case "Around_My_Rooms":
                mTitle = "05.Around_My_Rooms";
                break;
            case "Asia_Song":
                mTitle = "06.Asia_Song";
                break;
            case "Better_Weather":
                mTitle = "07.Better_Weather";
                break;
            case "Body_Bop":
                mTitle = "08.Body_Bop";
                break;
            case "Check_out_my_toy":
                mTitle = "09.Check_out_my_toy";
                break;
            case "City_Life_City_Lights":
                mTitle = "10.City_Life_City_Lights";
                break;
            case "Clean_and_Green":
                mTitle = "11.Clean_and_Green";
                break;
            case "Colourful_Party":
                mTitle = "12.Colourful_Party";
                break;
            case "Count_on_You":
                mTitle = "13.Count_on_You";
                break;
            case "Count_with_Bodhi":
                mTitle = "14.Count_with_Bodhi";
                break;
            case "Day_and_Night":
                mTitle = "15.Day_and_Night";
                break;
            case "Dream_Team":
                mTitle = "16.Dream_Team";
                break;
            case "Dress_You_Up":
                mTitle = "17.Dress_You_Up";
                break;
            case "Everyday_Heroes":
                mTitle = "18.Everyday_Heroes";
                break;
            case "Face_It":
                mTitle = "19.Face_It";
                break;
            case "Family":
                mTitle = "20.Family";
                break;
            case "Food":
                mTitle = "21.Food";
                break;
            case "Friends_Forever":
                mTitle = "22.Friends_Forever";
                break;
            case "Full_Circle":
                mTitle = "23.Full_Circle";
                break;
            case "Fun_Family_Day":
                mTitle = "24.Fun_Family_Day";
                break;
            case "Future_of_the_World":
                mTitle = "25.Future_of_the_World";
                break;
            case "Good_Food_Good_for_You":
                mTitle = "26.Good_Food_Good_for_You";
                break;
            case "I_Can_Do_It":
                mTitle = "27.I_Can_Do_It";
                break;
            case "I_Love_Food":
                mTitle = "28.I_Love_Food";
                break;
            case "Imagine":
                mTitle = "29.Imagine";
                break;
            case "In_Our_Neighbourhood":
                mTitle = "30.In_Our_Neighbourhood";
                break;
            case "Its_a_New_Day":
                mTitle = "31.Its_a_New_Day";
                break;
            case "Lets_Get_Fit":
                mTitle = "32.Lets_Get_Fit";
                break;
            case "Lets_Go_to_School":
                mTitle = "33.Lets_Go_to_School";
                break;
            case "Lets_Have_a_Good_Time":
                mTitle = "34.Lets_Have_a_Good_Time";
                break;
            case "Lets_Take_It_from_123":
                mTitle = "35.Lets_Take_It_from_123";
                break;
            case "Lets_play_ball":
                mTitle = "36.Lets_play_ball";
                break;
            case "Make_This_the_Best_Day":
                mTitle = "37.Make_This_the_Best_Day";
                break;
            case "Meet_Our_Families":
                mTitle = "38.Meet_Our_Families";
                break;
            case "Mix_and_Match":
                mTitle = "39.Mix_and_Match";
                break;
            case "Move_Our_Bodies":
                mTitle = "40.Move_Our_Bodies";
                break;
            case "My_Heart":
                mTitle = "41.My_Heart";
                break;
            case "My_Home":
                mTitle = "42.My_Home";
                break;
            case "No_Place_Like_Home":
                mTitle = "43.No_Place_Like_Home";
                break;
            case "Pat_Your_Pet":
                mTitle = "44.Pat_Your_Pet";
                break;
            case "Pet_Sounds":
                mTitle = "45.Pet_Sounds";
                break;
            case "Push_Through_Dont_Stop":
                mTitle = "46.Push_Through_Dont_Stop";
                break;
            case "Rainbow_Shining":
                mTitle = "47.Rainbow_Shining";
                break;
            case "Reap_What_You_Sow":
                mTitle = "48.Reap_What_You_Sow";
                break;
            case "School_Days":
                mTitle = "49.School_Days";
                break;
            case "See_you_later":
                mTitle = "50.See_you_later";
                break;
            case "Sharing_is_Caring":
                mTitle = "51.Sharing_is_Caring";
                break;
            case "Sing_Along_with_Bodhi_and_Friends":
                mTitle = "52.Sing_Along_with_Bodhi_and_Friends";
                break;
            case "So_Much_to_Do":
                mTitle = "53.So_Much_to_Do";
                break;
            case "Thats_What_We_Like":
                mTitle = "54.Thats_What_We_Like";
                break;
            case "The_Face_Song":
                mTitle = "55.The_Face_Song";
                break;
            case "The_Greetings_Song":
                mTitle = "56.The_Greetings_Song";
                break;
            case "The_Hello_Song":
                mTitle = "57.The_Hello_Song";
                break;
            case "The_Name_of_the_Game":
                mTitle = "58.The_Name_of_the_Game";
                break;
            case "The_Sounds_of_the_Farm":
                mTitle = "59.The_Sounds_of_the_Farm";
                break;
            case "The_time_is_now":
                mTitle = "60.The_time_is_now";
                break;
            case "Things_That_Go":
                mTitle = "61.Things_That_Go";
                break;
            case "Together":
                mTitle = "62.Together";
                break;
            case "Toys_toys_everywhere":
                mTitle = "63.Toys_toys_everywhere";
                break;
            case "We_Are_on_the_Go":
                mTitle = "64.We_Are_on_the_Go";
                break;
            case "We_Can_Be_Anything":
                mTitle = "65.We_Can_Be_Anything";
                break;
            case "We_ll_Be_Your_Shelter":
                mTitle = "66.We_ll_Be_Your_Shelter";
                break;
            case "Weather_Ready":
                mTitle = "67.Weather_Ready";
                break;
            case "Welcome_to_Our_World":
                mTitle = "68.Welcome_to_Our_World";
                break;
            case "Welcome_to_the_Farm":
                mTitle = "69.Welcome_to_the_Farm";
                break;
            case "When_I_Grow_Up":
                mTitle = "70.When_I_Grow_Up";
                break;
            case "With_My_Eyes":
                mTitle = "71.With_My_Eyes";
                break;
            case "Work_Hard_Get_Smart":
                mTitle = "72.Work_Hard_Get_Smart";
                break;
            // -----------------------宝狄英语----------------------------end

            //-----------------------多纳英语----------------------------start
            case "zaoshanghao":
                mTitle = "01.早上好";
                break;
            case "woshi":
                mTitle = "02.我是";
                break;
            case "meiweishuiguo":
                mTitle = "03.美味的水果";
                break;
            case "rangwomenshuyishu":
                mTitle = "04.我们数数";
                break;
            case "shitoujiandaobu":
                mTitle = "05.石头剪刀布";
                break;
            case "zhuomicang":
                mTitle = "06.捉迷藏";
                break;
            case "chuanshangyifu":
                mTitle = "07.穿上衣服";
                break;
            case "shangrikuaile":
                mTitle = "08.生日快乐";
                break;
            case "wotule":
                mTitle = "09.我涂了";
                break;
            case "moxojiamshenmeyanse":
                mTitle = "10.你喜欢什么颜色";
                break;
            case "qudongwuyuan":
                mTitle = "11.去动物园";
                break;
            case "keaidedongwu":
                mTitle = "12.可爱的动物";
                break;
            //-----------------------多纳英语----------------------------end---------强大的分割线

            //---------火火兔乐园---- 卡拉ok ---经典儿歌-----------------start
            case "fenshuajiang03":
                mTitle = "01.粉刷匠";
                break;
            case "yifenqian09":
                mTitle = "02.一分钱";
                break;
            case "woniuyuhuangliniao07":
                mTitle = "03.蜗牛与黄鹂鸟";
                break;
            case "lingerxiangdingdang04":
                mTitle = "04.铃儿响叮当";
                break;
            case "maliyouzhixiaoyanggao19":
                mTitle = "05.玛丽有只小羊羔";
                break;
            case "liangzhilaohu10":
                mTitle = "06.两只老虎";
                break;
            case "shishangzhiyoumamahao05":
                mTitle = "07.世上只有妈妈好";
                break;
            case "shuyazi06":
                mTitle = "数鸭子";
                break;
            case "aiwojiubaobaowo18":
                mTitle = "09.爱我你就抱抱我";
                break;
            case "baluobo15":
                mTitle = "10.拔萝卜";
                break;
            case "chuntianzainali01":
                mTitle = "11.春天在哪里";
                break;
            case "lanjinling02":
                mTitle = "12.蓝精灵";
                break;
            case "shijianzaishuohua17":
                mTitle = "13.时钟在说话";
                break;
            case "woyouyigejia13":
                mTitle = "14.我有一个家";
                break;
            case "xiaolongren12":
                mTitle = "15.小龙人";
                break;
            case "xiaoxingxing08":
                mTitle = "16.小星星";
                break;
            case "xiaoyanzi11":
                mTitle = "17.小燕子";
                break;
            case "zhaopengyou14":
                mTitle = "18.找朋友";
                break;
            case "zhongtaiyang20":
                mTitle = "19.种太阳";
                break;
            case "zhuoniqiu16":
                mTitle = "20.捉泥鳅";
                break;
            //---------火火兔乐园---- 卡拉ok ---经典儿歌-----------------end----------强大的分割线
            //---------火火兔乐园---- 卡拉ok ---原创儿歌-----------------start
            case "daxiongmao01":
                mTitle = "01.大熊猫";
                break;
            case "chunyu02":
                mTitle = "02.春雨";
                break;
            case "chaojiyingxiong03":
                mTitle = "03.超级英雄";
                break;
            case "zhuqingting04":
                mTitle = "04.竹蜻蜓";
                break;
            case "shangyuanzhuzhici05":
                mTitle = "05.上元竹枝词";
                break;
//经典儿歌 中的第八条重复
//            case "shuyazi06":
//                mTitle = "06.数鸭子";
//                break;
            case "yinghuoweiguang07":
                mTitle = "07.萤火微光";
                break;
            case "bahe08":
                mTitle = "08.拔河";
                break;
            case "hello09":
                mTitle = "09.hello";
                break;
            case "gongfuxiaozi10":
                mTitle = "10.功夫小子";
                break;
            case "xunyinzhibuyu11":
                mTitle = "11.寻隐者不遇";
                break;
            case "xiaoxiaobinggan12":
                mTitle = "12.小小饼干";
                break;
            case "xiaohuajia13":
                mTitle = "13.小画家";
                break;
            case "shancunyonghuai14":
                mTitle = "14.山村咏怀";
                break;
            case "gongxifacai15":
                mTitle = "15.恭喜发财";
                break;
            case "mingnong16":
                mTitle = "16.悯农";
                break;
            case "jinglige17":
                mTitle = "17.敬礼歌";
                break;
            case "chuntianzainali18":
                mTitle = "18.春天在哪里";
                break;
            case "zhishuge19":
                mTitle = "19.植树歌";
                break;
            case "aixiliangshi20":
                mTitle = "20.爱惜粮食";
                break;
            case "hua21":
                mTitle = "21.画";
                break;
            case "qiuyinong22":
                mTitle = "22.秋意浓";
                break;
            case "meilitianye23":
                mTitle = "23.美丽的田野";
                break;
            case "laoshukaihui24":
                mTitle = "24.老鼠开会";
                break;
            case "wuyixiang25":
                mTitle = "25.乌衣巷";
                break;
            case "daguonian26":
                mTitle = "26.过大年";
                break;
            //---------火火兔乐园---- 卡拉ok ---原创儿歌-----------------end----------强大的分割线

            //---------火火兔乐园---- 卡拉ok ---火火兔唱古诗-----------------start
            case "ye01":
                mTitle = "08.咏鹅";
                break;
            case "jys02":
                mTitle = "02.静夜思";
                break;
            case "yr03":
                mTitle = "03.元日";
                break;
            case "lzc04":
                mTitle = "04.凉州词";
                break;
            case "cs05":
                mTitle = "05.出塞";
                break;
            case "bdd06":
                mTitle = "06.别董大";
                break;
            case "glyx07":
                mTitle = "07.古朗月行";
                break;
            case "fdgyclb08":
                mTitle = "01.赋得古原草送别";
                break;
            case "xyzby09":
                mTitle = "09.寻隐者不遇";
                break;
            case "xc10":
                mTitle = "10.小池";
                break;
            case "sx11":
                mTitle = "11.山行";
                break;
            case "jhzs12":
                mTitle = "12.己亥杂诗";
                break;
            case "zfbdc13":
                mTitle = "13.早发白帝城";
                break;
            case "zccsb14":
                mTitle = "14.早春呈水部张十八员外";
                break;
            case "cyxy15":
                mTitle = "15.春夜喜雨";
                break;
            case "cx16":
                mTitle = "16.春晓";
                break;
            case "wlspb17":
                mTitle = "17.望庐山瀑布";
                break;
            case "fqyb18":
                mTitle = "18.枫桥夜泊";
                break;
            case "jpdbxh19":
                mTitle = "19.江畔独步寻花";
                break;
            case "jx20":
                mTitle = "20.江雪";
                break;
            case "yzy21":
                mTitle = "21.游子吟";
                break;
            case "dgql22":
                mTitle = "22.登鹳雀楼";
                break;
            case "jj23":
                mTitle = "23.绝句";
                break;
            case "cgx24":
                mTitle = "24.长歌行";
                break;

            //---------火火兔乐园---- 卡拉ok ---火火兔唱古诗-----------------end----------强大的分割线
            //---------火火兔乐园---- 艺术培养 -------艺术之旅----------start---------
            case "sjmh1":
                mTitle = "世界名画1";
                break;
            case "sjmh2":
                mTitle = "世界名画2";
                break;
            case "sjmh3":
                mTitle = "世界名画3";
                break;
            case "sjmh4":
                mTitle = "世界名画4";
                break;
            case "sjmh5":
                mTitle = "世界名画5";
                break;
            case "zmds":
                mTitle = "著名雕塑欣赏";
                break;
            //---------火火兔乐园---- 艺术培养 --------艺术之旅---------end---------强大的分割线

            //---------火火兔乐园---- 艺术培养 -------小小画家----------start---------
            case "xiaopangxie01":
                mTitle = "01.小螃蟹";
                break;
            case "xiaomifeng02":
                mTitle = "02.小蜜蜂";
                break;
            case "xiaomuji03":
                mTitle = "03.小母鸡";
                break;
            case "xiaoniunai04":
                mTitle = "04.小奶牛";
                break;
            case "xiaoyu05":
                mTitle = "05.小鱼";
                break;
            case "xiaohouzi06":
                mTitle = "06.小猴子";
                break;
            case "xiaozhu07":
                mTitle = "07.小猪";
                break;
            case "xiaohuamao08":
                mTitle = "08.小花猫";
                break;
            case "changjinglu09":
                mTitle = "09.小长颈鹿";
                break;
            case "jijiubao10":
                mTitle = "10.急救包";
                break;
            case "qingwa11":
                mTitle = "11.青蛙";
                break;
            case "luantuluanhua12":
                mTitle = "12.乱涂乱画";
                break;
            case "caihong13":
                mTitle = "13.彩虹";
                break;
            case "aihusongling14":
                mTitle = "14.爱护树林";
                break;
            case "shuomohua15":
                mTitle = "15.水墨画";
                break;
            case "woxiangyaozhangda16":
                mTitle = "16.我想要长大";
                break;
            case "yuzhou17":
                mTitle = "17.宇宙";
                break;
            case "dagongji18":
                mTitle = "18.大公鸡";
                break;
            //---------火火兔乐园---- 艺术培养 -------小小画家----------edn--------强大的分割线

            //---------火火兔乐园---- 艺术培养 -------律动儿歌----------start---------

            case "lingerxiangdingdang01":
                mTitle = "01.铃儿响叮当";
                break;
            case "bingtanghulu02":
                mTitle = "02.冰糖葫芦";
                break;
            case "paizhaopian03":
                mTitle = "03.拍照片";
                break;
            case "yinghuoweiguang04":
                mTitle = "04.萤火微光";
                break;
            case "gongxifacai05":
                mTitle = "05.恭喜发财鸡";
                break;
            case "gudonglaile06":
                mTitle = "06.叮咚来了";
                break;
            case "zhiqingting07":
                mTitle = "07.竹蜻蜓";
                break;
            case "chaojiyingxiong08":
                mTitle = "08.超级英雄";
                break;
            case "xuanzhuanmuma09":
                mTitle = "09.旋转木马";
                break;
            case "yundongge10":
                mTitle = "10.运动歌";
                break;
            case "shuyazi11":
                mTitle = "11.数鸭子";
                break;
            case "naozhongxiangdingdang12":
                mTitle = "12.闹铃叮当响";
                break;
            case "huanlehht13":
                mTitle = "13.欢乐火火兔";
                break;
            //---------火火兔乐园---- 艺术培养 -------律动儿歌----------edn--------强大的分割线


        }
        return mTitle;
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
            if (accompanyLayout != null && accompanyLayout.getVisibility() == GONE) {
                accompanyLayout.setVisibility(VISIBLE);
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
            ibPlay.setImageResource(R.drawable.mediacontorller_pause);
//      mPauseButton.setImageResource(getResources().getIdentifier("mediacontroller_pause", "drawable", mContext.getPackageName()));
        else
            ibPlay.setImageResource(R.drawable.mediacontorller_play);
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
            if (TextUtils.equals(MICROPHONE_ACTION, action)) {
                if (intent.getIntExtra("state", 0) == 1) {//拔出
//                    if (ivMicrophone != null) {
//                        ivMicrophone.setVisibility(GONE);
//                    }

                } else if (intent.getIntExtra("state", 0) == 0) {// 插入
//                    if (ivMicrophone != null) {
//                        ivMicrophone.setVisibility(VISIBLE);
//                    }
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


//    private void initVolumeView(View volumeView) {
//        volume_progress = (SeekBar) volumeView.findViewById(R.id.seekbar_volume);
//        tvProgress = (TextView) volumeView.findViewById(R.id.tv_progress);
//        final AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        //最大音量
//        final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        //当前音量
//        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        int currentVolumeProgress = (int) ((float) currentVolume * 100 / maxVolume);
//        tvProgress.setText(currentVolumeProgress + "/100");
//        volume_progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                tvProgress.setText(progress + "/100");
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                mHandler.removeMessages(FADE_OUT);
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                int progress = seekBar.getProgress();
//                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress * maxVolume / 100, AudioManager.FX_KEY_CLICK);
//                show();
//            }
//        });
//    }

    public void setVolume() {
//        final AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        //最大音量
//        final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        //当前音量
//        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
////        int currentVolumeProgress = (int) ((float) currentVolume * 100 / maxVolume);
//        currentVolume = currentVolume + 1;
//        if (currentVolume > maxVolume) {
//            currentVolume = currentVolume - 2;
//        }
//        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FX_KEY_CLICK);
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
