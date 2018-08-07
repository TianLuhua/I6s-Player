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


        switch (mTitle) {
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
