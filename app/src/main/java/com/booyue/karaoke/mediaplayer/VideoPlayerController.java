//package com.booyue.karaoke.mediaplayer;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnCompletionListener;
//import android.media.MediaPlayer.OnErrorListener;
//import android.media.MediaPlayer.OnPreparedListener;
//import android.media.MediaPlayer.TrackInfo;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnTouchListener;
//import android.widget.FrameLayout;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.SeekBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;
//import android.widget.TextView;
//
//import com.tigercxvideo.R;
//import com.tigercxvideo.model.MediaPlayData;
//import com.tigercxvideo.util.SharedPreferencUtil;
//
//import java.util.Formatter;
//import java.util.List;
//import java.util.Locale;
//
//public class VideoPlayerController extends FrameLayout implements OnClickListener, OnTouchListener {
//    private static final int CHANGE_MODE = 3;
//    private static final int DEFAUL_SHOW_TIME = 3000;
//    private static final int DEFAUL_TTIME_OUT = 5000;
//    private static final int FADE_OUT = 1;
//    private static final int SEEKBAR_SHOW_TIME = 3600000;
//    private static final String SHARE_APP_TAG = null;
//    private static final int SHOW_PROGRESS = 2;
//    private int cirType = 0;
//    private boolean curPriginal = true;
//    private boolean getisVideo = false;
//    private boolean isHide = false;
//    private boolean isPriginal = true;
//    private boolean iszhixian = true;
//    private LinearLayout lin_bg;
//    private ImageView loop_btn;
//    private ImageButton mAccompany;
//    private AudioManager mAudioManager;
//    private boolean mAutoHideController = true;
//    private List<MediaPlayData.SrcBean> mBeans;
//    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
//        public void onCompletion(MediaPlayer paramMediaPlayer) {
//            Log.i(AppVideoSurfaceView.TAG, "OnCompletionListener  ");
//            VideoPlayerController.this.updatePlayPauseState();
//        }
//    };
//    private Context mContext;
//    private boolean mDragging;
//    private long mDuration;
//    private TextView mDurationText;
//    private StringBuilder mFormatBuilder;
//    private Formatter mFormatter;
//    private final Handler mHandler = new Handler() {
//        public void handleMessage(Message paramMessage) {
//            switch (paramMessage.what) {
//                default:
//                case 1:
//                case 2:
//                case 3:
//            }
//            do {
//                do {
//                    long l;
//                    do {
//                        while (true) {
//                            return;
//                            if (!VideoPlayerController.this.mAutoHideController)
//                                continue;
//                            Log.e("cao", "mAutoHideController==" + VideoPlayerController.this.mAutoHideController);
//                            VideoPlayerController.this.hide();
//                            return;
//                            if (!VideoPlayerController.this.mMediaPlayerControl.getisMediaPlayertype())
//                                break;
//                            l = VideoPlayerController.this.setProgress();
//                            if ((VideoPlayerController.this.mDragging) || (!VideoPlayerController.this.mShowing))
//                                continue;
//                            sendMessageDelayed(obtainMessage(2), 1000L - l % 1000L);
//                            return;
//                        }
//                        l = VideoPlayerController.this.setProgress2();
//                    }
//                    while ((VideoPlayerController.this.mDragging) || (!VideoPlayerController.this.mShowing));
//                    sendMessageDelayed(obtainMessage(2), 1000L - l % 1000L);
//                    return;
//                    Log.i("cao", "改变");
//                }
//                while ((VideoPlayerController.this.mTrack <= 1) || (VideoPlayerController.this.isPriginal));
//                Log.e("cao", VideoPlayerController.this.getTrackIndex(false));
//            }
//            while (((Boolean) paramMessage.obj).booleanValue() != VideoPlayerController.this.curPriginal);
//            VideoPlayerController.this.mMediaPlayerControl.selectTrack(VideoPlayerController.this.getTrackIndex(false));
//        }
//    };
//    private OnHiddenListener mHiddenListener;
//    private TrackInfo[] mInfos;
//    private long mMediaDuration;
//    private MediaPlayerControl mMediaPlayerControl;
//    private int mMediaPosition;
//    private int mMediaPositionsize;
//    private ImageView mNextButton;
//    private OnErrorListener mOnErrorListener = new OnErrorListener() {
//        public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
//            Log.i(TAG, "OnErrorListener  ");
//            VideoPlayerController.this.updatePlayPauseState();
//            return true;
//        }
//    };
//    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
//        public void onPrepared(MediaPlayer paramMediaPlayer) {
//            StringBuilder localStringBuilder = new StringBuilder("AppVideoControllerView mPreparedListener-> onPrepared is UI thread?");
//            boolean bool;
//            label77:
//            int i;
//            if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
//                bool = true;
//                Log.i((String) localObject, bool);
//                if (!VideoPlayerController.this.mMediaPlayerControl.getisMediaPlayertype())
//                    break label197;
//                VideoPlayerController.this.setProgress();
//                VideoPlayerController.this.mTrack = 0;
//                VideoPlayerController.this.mInfos = paramMediaPlayer.getTrackInfo();
//                System.out.println("showPrigAccom 22 mTrack " + VideoPlayerController.this.mTrack);
//                paramMediaPlayer = VideoPlayerController.this.mInfos;
//                int j = paramMediaPlayer.length;
//                i = 0;
//                label137:
//                if (i < j)
//                    break label208;
//                if (VideoPlayerController.this.mTrack > 1) {
//                    if (!VideoPlayerController.this.isPriginal)
//                        break label296;
//                    VideoPlayerController.this.showPrigAccom(true);
//                }
//            }
//            while (true) {
//                VideoPlayerController.this.mMediaPlayerControl.play();
//                VideoPlayerController.this.updatePlayPauseState();
//                return;
//                bool = false;
//                break;
//                label197:
//                VideoPlayerController.this.setProgress2();
//                break label77;
//                label208:
//                localObject = paramMediaPlayer[i];
//                System.out.println("showPrigAccom 33 mTrack " + VideoPlayerController.this.mTrack +
//                        ((TrackInfo) localObject).getTrackType() + "--" + ((TrackInfo) localObject).getLanguage());
//                if (2 == ((TrackInfo) localObject).getTrackType()) {
//                    localObject = VideoPlayerController.this;
//                    ((VideoPlayerController) localObject).mTrack += 1;
//                }
//                i += 1;
//                break label137;
//                label296:
//                VideoPlayerController.this.showPrigAccom(false);
//                paramMediaPlayer = VideoPlayerController.this.mHandler.obtainMessage(3);
//                paramMediaPlayer.obj = Boolean.valueOf(false);
//                VideoPlayerController.this.curPriginal = false;
//                VideoPlayerController.this.mHandler.sendMessageDelayed(paramMediaPlayer, 300L);
//            }
//        }
//    };
//    private OnPreviousNextListenr mOnPreviousNextListenr;
//    private OnSeekBarCallback mOnSeekBarCallback;
//    private final OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
//        public void onProgressChanged(SeekBar paramSeekBar, int paramInt, boolean paramBoolean) {
//            if ((!paramBoolean) || (VideoPlayerController.this.mMediaPlayerControl == null)) ;
//            do {
//                while (true) {
//                    return;
//                    if (!VideoPlayerController.this.mMediaPlayerControl.getisMediaPlayertype())
//                        break;
//                    VideoPlayerController.this.mDuration = VideoPlayerController.this.mMediaPlayerControl.getDuration();
//                    l = VideoPlayerController.this.mDuration * paramInt / paramSeekBar.getMax();
//                    Log.i(AppVideoSurfaceView.TAG, "onProgressChanged  " + l + "--" + VideoPlayerController.this.mDuration + "--" + paramSeekBar.getMax());
//                    if (VideoPlayerController.this.mPositionText != null)
//                        VideoPlayerController.this.mPositionText.setText(VideoPlayerController.this.stringForTime(l));
//                    if (VideoPlayerController.this.mDurationText == null)
//                        continue;
//                    VideoPlayerController.this.mDurationText.setText(VideoPlayerController.this.stringForTime(VideoPlayerController.this.mDuration));
//                    return;
//                }
//                VideoPlayerController.this.mDuration = VideoPlayerController.this.mMediaPlayerControl.getmDuration();
//                long l = VideoPlayerController.this.mDuration * paramInt / paramSeekBar.getMax();
//                VideoPlayerController.this.mMediaPlayerControl.seekTo((int) l);
//                if (VideoPlayerController.this.mPositionText == null)
//                    continue;
//                VideoPlayerController.this.mPositionText.setText(VideoPlayerController.this.stringForTime(VideoPlayerController.this.mMediaPlayerControl.getCurrentPosition()));
//            }
//            while (VideoPlayerController.this.mDurationText == null);
//            VideoPlayerController.this.mDurationText.setText(VideoPlayerController.this.stringForTime(VideoPlayerController.this.mDuration));
//        }
//
//        public void onStartTrackingTouch(SeekBar paramSeekBar) {
//            Log.i(AppVideoSurfaceView.TAG, "onStartTrackingTouch  ");
//            VideoPlayerController.this.mDragging = true;
//            VideoPlayerController.this.show(3600000);
//            VideoPlayerController.this.mHandler.removeMessages(2);
//            if (VideoPlayerController.this.mOnSeekBarCallback != null)
//                VideoPlayerController.this.mOnSeekBarCallback.onSeekbarStartTracking(paramSeekBar);
//        }
//
//        public void onStopTrackingTouch(SeekBar paramSeekBar) {
//            if (VideoPlayerController.this.mMediaPlayerControl == null) ;
//            do {
//                return;
//                if (VideoPlayerController.this.mMediaPlayerControl.getisMediaPlayertype()) {
//                    VideoPlayerController.this.mDuration = VideoPlayerController.this.mMediaPlayerControl.getDuration();
//                    Log.i(AppVideoSurfaceView.TAG, "onStopTrackingTouch  " + paramSeekBar.getMax() + "--" + paramSeekBar.getProgress() + "--" + VideoPlayerController.this.mDuration);
//                    l1 = VideoPlayerController.this.mDuration * paramSeekBar.getProgress() / paramSeekBar.getMax();
//                    VideoPlayerController.this.mnewDuration = l1;
//                    VideoPlayerController.this.mMediaPosition = VideoPlayerController.this.isSrcBean((int) l1);
//                    long l2 = VideoPlayerController.this.isSrcBeanconect(VideoPlayerController.this.mMediaPosition);
//                    VideoPlayerController.this.mMediaDuration = (l1 - l2);
//                    Log.i(AppVideoSurfaceView.TAG, "isSrcBean newposition=33=  " + l1 + "--" + VideoPlayerController.this.mMediaPosition);
//                    if (VideoPlayerController.this.mPathType == VideoPlayerController.this.mMediaPosition)
//                        VideoPlayerController.this.mMediaPlayerControl.seekTo((int) VideoPlayerController.this.mMediaDuration);
//                    while (true) {
//                        VideoPlayerController.this.show(5000);
//                        VideoPlayerController.this.mHandler.removeMessages(2);
//                        VideoPlayerController.this.mDragging = false;
//                        VideoPlayerController.this.mHandler.sendEmptyMessageDelayed(2, 3000L);
//                        if (VideoPlayerController.this.mOnSeekBarCallback == null)
//                            break;
//                        VideoPlayerController.this.mOnSeekBarCallback.onSeekBarStopTracking(paramSeekBar);
//                        return;
//                        Log.i(AppVideoSurfaceView.TAG, "isSrcBean=33=  " + VideoPlayerController.this.mMediaPosition + "----" + VideoPlayerController.this.mMediaDuration);
//                        VideoPlayerController.this.getisVideo = true;
//                        VideoPlayerController.this.mMediaPlayerControl.completionToNext();
//                    }
//                }
//                VideoPlayerController.this.mDuration = VideoPlayerController.this.mMediaPlayerControl.getmDuration();
//                Log.i(AppVideoSurfaceView.TAG, "onStopTrackingTouch  " + paramSeekBar.getMax() + "--" + paramSeekBar.getProgress() + "--" + VideoPlayerController.this.mDuration);
//                long l1 = VideoPlayerController.this.mDuration * paramSeekBar.getProgress() / paramSeekBar.getMax();
//                VideoPlayerController.this.mMediaPlayerControl.seekTo((int) l1);
//                VideoPlayerController.this.show(5000);
//                VideoPlayerController.this.mHandler.removeMessages(2);
//                VideoPlayerController.this.mDragging = false;
//                VideoPlayerController.this.mHandler.sendEmptyMessageDelayed(2, 3000L);
//            }
//            while (VideoPlayerController.this.mOnSeekBarCallback == null);
//            VideoPlayerController.this.mOnSeekBarCallback.onSeekBarStopTracking(paramSeekBar);
//        }
//    };
//    private int mPathType;
//    private ImageButton mPauseButton;
//    private ImageButton mPlayButton;
//    private TextView mPositionText;
//    private ImageView mPreviousButton;
//    private ImageButton mPriginal;
//    private SeekBar mProgressBar;
//    private boolean mShowing = true;
//    private OnShownListener mShownListener;
//    private int mTrack = 0;
//    private long mnewDuration;
//    private RelativeLayout rLayout;
//    public VideoFragment.VisibilityShow vf = new VideoFragment.VisibilityShow() {
//        public void onShow() {
//            VideoPlayerController.this.lin_bg.setVisibility(0);
//        }
//    };
//    private VisibilityListener visibilityListener;
//    private ImageView voice_btn;
//
//    public VideoPlayerController(Context paramContext) {
//        this(paramContext, null);
//    }
//
//    public VideoPlayerController(Context paramContext, AttributeSet paramAttributeSet) {
//        this(paramContext, paramAttributeSet, 0);
//    }
//
//    public VideoPlayerController(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
//        super(paramContext, paramAttributeSet, paramInt);
//        this.mContext = paramContext;
//        isFirst();
//        initView();
//        initData();
//    }
//
//    private int getTrackIndex(boolean paramBoolean) {
//        int j = 0;
//        int i = 0;
//        while (true) {
//            int k;
//            if (i >= this.mInfos.length)
//                k = j;
//            do {
//                return k;
//                if (2 != this.mInfos[i].getTrackType())
//                    break;
//                j = i;
//                k = j;
//            }
//            while (paramBoolean);
//            i += 1;
//        }
//    }
//
//    private void hidePrigAccom() {
//        this.mPriginal.setVisibility(GONE);
//        this.mAccompany.setVisibility(GONE);
//    }
//
//    private void initData() {
//        if (SharedPreferencUtil.getBoolean("isdefault")) {
//            this.loop_btn.setImageResource(2130837521);
//            VideoFragment.IsSingle = true;
//            return;
//        }
//        this.loop_btn.setImageResource(2130837549);
//        VideoFragment.IsSingle = false;
//    }
//
//    private void initView() {
//        View localView = LayoutInflater.from(this.mContext).inflate(R.layout.app_video_controller_view, this);
//        this.mAudioManager = ((AudioManager) this.mContext.getSystemService(Context.AUDIO_SERVICE));
//        this.lin_bg = ((LinearLayout) localView.findViewById(2131361828));
//        this.lin_bg.setOnTouchListener(this);
//        this.mPositionText = ((TextView) localView.findViewById(2131361801));
//        this.mDurationText = ((TextView) localView.findViewById(2131361795));
//        this.mProgressBar = ((SeekBar) localView.findViewById(2131361803));
//        this.mProgressBar.setOnSeekBarChangeListener(this.mOnSeekBarChangeListener);
//        this.mProgressBar.setMax(1000);
//        this.mPlayButton = ((ImageButton) localView.findViewById(2131361800));
//        this.mPlayButton.setOnClickListener(this);
//        this.mPauseButton = ((ImageButton) localView.findViewById(2131361799));
//        this.mPauseButton.setOnClickListener(this);
//        this.loop_btn = ((ImageView) localView.findViewById(2131361829));
//        this.loop_btn.setOnClickListener(this);
//        this.voice_btn = ((ImageView) localView.findViewById(2131361830));
//        this.voice_btn.setOnClickListener(this);
//        this.mFormatBuilder = new StringBuilder();
//        this.mFormatter = new Formatter(this.mFormatBuilder, Locale.getDefault());
//        this.mPreviousButton = ((ImageView) localView.findViewById(2131361802));
//        this.mPreviousButton.setOnClickListener(this);
//        this.mNextButton = ((ImageView) localView.findViewById(2131361797));
//        this.mNextButton.setOnClickListener(this);
//        this.mPriginal = localView.findViewById(R.id.exo_original_singer);
//        this.mAccompany = localView.findViewById(R.id.exo_accompany_singer);
//        this.mPriginal.setOnClickListener(this);
//        this.mAccompany.setOnClickListener(this);
//        hidePrigAccom();
//    }
//
//    private void isFirst() {
//        SharedPreferences localSharedPreferences = this.mContext.getSharedPreferences(SHARE_APP_TAG, 0);
//        if (Boolean.valueOf(localSharedPreferences.getBoolean("FIRST", true)).booleanValue()) {
//            SharedPreferencUtil.setBoolean("isdefault", false);
//            localSharedPreferences.edit().putBoolean("FIRST", false).commit();
//        }
//    }
//
//    private long setProgress() {
//        if (this.mMediaPlayerControl == null)
//            return 0L;
//        long l2 = this.mMediaPositionsize;
//        long l1 = this.mMediaPlayerControl.getCurrentPosition();
//        long l3 = this.mProgressBar.getMax();
//        this.mDuration = l2;
//        if (this.mMediaPosition > 0)
//            l1 += isSrcBeanconect(this.mMediaPlayerControl.getselectIndex());
//        while (true) {
//            Log.i(AppVideoSurfaceView.TAG, "setProgress=11= ---" + l1 + "--" + this.mMediaPositionsize + "--" + this.mMediaPlayerControl.getselectIndex() + "---" + this.mMediaPosition);
//            if (this.mProgressBar != null)
//                this.mProgressBar.setProgress((int) (l1 * l3 / l2));
//            if (this.mDurationText != null)
//                this.mDurationText.setText(stringForTime(this.mDuration));
//            if (this.mPositionText != null)
//                this.mPositionText.setText(stringForTime(l1));
//            this.mProgressBar.setVisibility(0);
//            this.mPositionText.setVisibility(0);
//            this.mDurationText.setVisibility(0);
//            return l1;
//            l1 += isSrcBeanconect(this.mMediaPlayerControl.getselectIndex());
//        }
//    }
//
//    private long setProgress2() {
//        if (this.mMediaPlayerControl == null)
//            return 0L;
//        long l1;
//        long l2;
//        long l3;
//        if (this.mAudioManager.getStreamVolume(3) == 0) {
//            this.voice_btn.setImageResource(2130837550);
//            l1 = this.mMediaPlayerControl.getmDuration();
//            l2 = this.mMediaPlayerControl.getCurrentPosition();
//            if (l1 <= 0L)
//                break label288;
//            l3 = 1000L * l2 / l1;
//        }
//        label288:
//        while (true) {
//            l3 = this.mMediaPlayerControl.getBufferPercentage();
//            long l4 = this.mProgressBar.getMax();
//            this.mDuration = l1;
//            Log.i(AppVideoSurfaceView.TAG, "setProgress== " + l2 * l4 / l1 + "--" + l2 + "---" + l1 + "--" + l4);
//            Log.i(AppVideoSurfaceView.TAG, "setProgress=11= " + l1);
//            if (this.mProgressBar != null) {
//                this.mProgressBar.setProgress((int) (l2 * l4 / l1));
//                this.mProgressBar.setSecondaryProgress((int) l3 * 10);
//            }
//            if (this.mDurationText != null)
//                this.mDurationText.setText(stringForTime(l1));
//            if (this.mPositionText != null)
//                this.mPositionText.setText(stringForTime(l2));
//            this.mProgressBar.setVisibility(0);
//            this.mPositionText.setVisibility(0);
//            this.mDurationText.setVisibility(0);
//            return l2;
//            this.voice_btn.setImageResource(2130837560);
//            break;
//        }
//    }
//
//    private void show(int paramInt) {
//        if (this.mShowing) ;
//        while (true) {
//            return;
//            Log.e(AppVideoSurfaceView.TAG, "AppVideoControllerView:show:" + paramInt);
//            try {
//                setVisibility(0);
//                if (this.visibilityListener != null)
//                    this.visibilityListener.onVisibilityChange(0, false);
//                this.mHandler.sendEmptyMessage(2);
//                if (paramInt != 0) {
//                    this.mHandler.removeMessages(1);
//                    if (this.mAutoHideController)
//                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), paramInt);
//                }
//                this.mShowing = true;
//                if (this.mShownListener == null)
//                    continue;
//                this.mShownListener.onShown();
//                return;
//            } catch (IllegalArgumentException localIllegalArgumentException) {
//                while (true)
//                    Log.e(AppVideoSurfaceView.TAG, "AppVideoControllerView show():" + localIllegalArgumentException.toString());
//            }
//        }
//    }
//
//    private void showPrigAccom(boolean paramBoolean) {
//        System.out.println("showPrigAccom " + paramBoolean);
//        if (this.mTrack < 2)
//            return;
//        System.out.println("showPrigAccom mTrack " + this.mTrack);
//        if (paramBoolean) {
//            this.mPriginal.setVisibility(VISIBLE);
//            this.mAccompany.setVisibility(GONE);
//            this.isPriginal = true;
//            return;
//        }
//        this.mPriginal.setVisibility(GONE);
//        this.mAccompany.setVisibility(VISIBLE);
//        this.isPriginal = false;
//    }
//
//    private void showPrigAccom2(boolean paramBoolean) {
//        System.out.println("showPrigAccom " + paramBoolean);
//        if (this.mTrack < 3)
//            return;
//        System.out.println("showPrigAccom mTrack " + this.mTrack);
//    }
//
//    private String stringForTime(long paramLong) {
//        int k = (int) (paramLong / 1000L);
//        int i = k % 60;
//        int j = k / 60 % 60;
//        k /= 3600;
//        this.mFormatBuilder.setLength(0);
//        if (k > 0)
//            return this.mFormatter.format("%d:%02d:%02d", new Object[]{Integer.valueOf(k), Integer.valueOf(j), Integer.valueOf(i)}).toString();
//        return this.mFormatter.format("%02d:%02d", new Object[]{Integer.valueOf(j), Integer.valueOf(i)}).toString();
//    }
//
//    private void updatePlayPauseState() {
//        if ((this.mPauseButton == null) || (this.mPlayButton == null) || (this.mMediaPlayerControl == null))
//            return;
//        if (this.mMediaPlayerControl.isPlaying()) {
//            this.mPauseButton.setVisibility(0);
//            this.mPlayButton.setVisibility(8);
//            return;
//        }
//        this.mPauseButton.setVisibility(8);
//        this.mPlayButton.setVisibility(0);
//    }
//
//    public boolean dispatchKeyEvent(KeyEvent paramKeyEvent) {
//        System.out.println("dispatchKeyEvent++ " + paramKeyEvent + " " + paramKeyEvent.getAction());
//        if (1 == paramKeyEvent.getAction()) {
//            int i = paramKeyEvent.getKeyCode();
//            if (i == 87)
//                onClick(this.mNextButton);
//            while (true) {
//                return true;
//                if (i == 88) {
//                    onClick(this.mPreviousButton);
//                    return true;
//                }
//                if (i != 85)
//                    break;
//                if ((this.mPauseButton != null) && (this.mPauseButton.getVisibility() == 0)) {
//                    onClick(this.mPauseButton);
//                    return true;
//                }
//                if (this.mPlayButton == null)
//                    continue;
//                onClick(this.mPlayButton);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void exit() {
//        this.mHandler.removeMessages(1);
//        this.mHandler.removeMessages(2);
//    }
//
//    public long getMediaDuration() {
//        return this.mMediaDuration;
//    }
//
//    public int getMediaProgress() {
//        return this.mMediaPosition;
//    }
//
//    public boolean getisVideo() {
//        return this.getisVideo;
//    }
//
//    public void hide() {
//        if (!this.mShowing) ;
//        while (true) {
//            return;
//            Log.e(AppVideoSurfaceView.TAG, "AppVideoControllerView:hide");
//            try {
//                setVisibility(8);
//                if (this.visibilityListener != null)
//                    this.visibilityListener.onVisibilityChange(8, false);
//                this.mHandler.removeMessages(2);
//                this.mShowing = false;
//                if (this.mHiddenListener == null)
//                    continue;
//                this.mHiddenListener.onHidden();
//                return;
//            } catch (IllegalArgumentException localIllegalArgumentException) {
//                while (true)
//                    Log.e(AppVideoSurfaceView.TAG, "AppVideoControllerView hide():" + localIllegalArgumentException.toString());
//            }
//        }
//    }
//
//    public boolean isShowing() {
//        return this.mShowing;
//    }
//
//    public int isSrcBean(int paramInt) {
//        Float localFloat = Float.valueOf(0.0F);
//        int i = 0;
//        while (true) {
//            if (i >= this.mBeans.size())
//                return 0;
//            float f = localFloat.floatValue();
//            localFloat = Float.valueOf(Float.valueOf(((MediaPlayData.SrcBean) this.mBeans.get(i)).getVideo()).floatValue() * 1000.0F + f);
//            Log.i(AppVideoSurfaceView.TAG, "isSrcBean=11=  " + ((MediaPlayData.SrcBean) this.mBeans.get(i)).getVideo() + "--" + localFloat);
//            if (localFloat.floatValue() > paramInt) {
//                Log.i(AppVideoSurfaceView.TAG, "isSrcBean=11=  " + localFloat + "---" + paramInt + "----" + i);
//                return i;
//            }
//            i += 1;
//        }
//    }
//
//    public long isSrcBeanconect(int paramInt) {
//        float f = 0.0F;
//        int i = 0;
//        while (true) {
//            if (i >= paramInt)
//                return () f;
//            f += Float.valueOf(((MediaPlayData.SrcBean) this.mBeans.get(i)).getVideo()).floatValue() * 1000.0F;
//            i += 1;
//        }
//    }
//
//    public void onClick(View paramView) {
//        if (paramView == this.mPauseButton) {
//            this.mMediaPlayerControl.pause();
//            updatePlayPauseState();
//        }
//        do {
//            while (true) {
//                return;
//                if (paramView == this.mPlayButton) {
//                    this.mMediaPlayerControl.play();
//                    updatePlayPauseState();
//                    return;
//                }
//                if (paramView == this.mPreviousButton) {
//                    Log.e("cao", "isPriginal==" + this.isPriginal + "mTrack" + this.mTrack);
//                    if (this.mOnPreviousNextListenr == null)
//                        continue;
//                    this.mOnPreviousNextListenr.previous();
//                    return;
//                }
//                if (paramView == this.mNextButton) {
//                    if (this.mOnPreviousNextListenr == null)
//                        continue;
//                    this.mOnPreviousNextListenr.next();
//                    return;
//                }
//                if (paramView.getId() == R.id.exo_original_singer) {
//                    this.mHandler.removeMessages(3);
//                    this.curPriginal = true;
//                    showPrigAccom(false);
//                    if (this.mTrack <= 1)
//                        continue;
//                    this.mMediaPlayerControl.selectTrack(getTrackIndex(false));
//                    return;
//                }
//                if (paramView.getId() != 2131361833)
//                    break;
//                this.mHandler.removeMessages(3);
//                this.curPriginal = true;
//                showPrigAccom(true);
//                if (this.mTrack <= 1)
//                    continue;
//                this.mMediaPlayerControl.selectTrack(getTrackIndex(true));
//                return;
//            }
//            if (paramView.getId() == 2131361828) {
//                Log.i("cao", "bottom_layout");
//                this.mHandler.sendEmptyMessageAtTime(1, 3600000L);
//                return;
//            }
//            if (paramView.getId() != 2131361829)
//                continue;
//            if (SharedPreferencUtil.getBoolean("isdefault")) {
//                SharedPreferencUtil.setBoolean("isdefault", false);
//                this.loop_btn.setImageResource(2130837549);
//                VideoFragment.IsSingle = false;
//                return;
//            }
//            this.loop_btn.setImageResource(2130837521);
//            SharedPreferencUtil.setBoolean("isdefault", true);
//            VideoFragment.IsSingle = true;
//            return;
//        }
//        while (paramView.getId() != 2131361830);
//        if (this.mAudioManager.getStreamVolume(3) == 0)
//            this.voice_btn.setImageResource(2130837550);
//        while (true) {
//            Log.e("cao", "voice");
//            this.mAudioManager.setStreamVolume(3, this.mAudioManager.getStreamVolume(3), 1);
//            return;
//            this.voice_btn.setImageResource(2130837560);
//        }
//    }
//
//    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
//        switch (paramView.getId()) {
//            default:
//                return false;
//            case 2131361828:
//        }
//        Log.e("cao", "onTouch==" + paramView);
//        this.mShowing = false;
//        return false;
//    }
//
//    public void setHiddenListener(OnHiddenListener paramOnHiddenListener) {
//        this.mHiddenListener = paramOnHiddenListener;
//    }
//
//    public void setMediaPlayerControl(MediaPlayerControl paramMediaPlayerControl) {
//        this.mMediaPlayerControl = paramMediaPlayerControl;
//        paramMediaPlayerControl.addOnPreparedListener(this.mOnPreparedListener);
//        paramMediaPlayerControl.addOnCompletionListener(this.mCompletionListener);
//        paramMediaPlayerControl.addOnErrorListener(this.mOnErrorListener);
//    }
//
//    public void setOnPreviousNextListenr(OnPreviousNextListenr paramOnPreviousNextListenr) {
//        this.mOnPreviousNextListenr = paramOnPreviousNextListenr;
//    }
//
//    public void setOnSeekBarCallback(OnSeekBarCallback paramOnSeekBarCallback) {
//        this.mOnSeekBarCallback = paramOnSeekBarCallback;
//    }
//
//    public void setShownListener(OnShownListener paramOnShownListener) {
//        this.mShownListener = paramOnShownListener;
//    }
//
//    public void setSrcBean(List<MediaPlayData.SrcBean> paramList, int paramInt1, int paramInt2) {
//        int j = 0;
//        int i = 0;
//        while (true) {
//            if (i >= paramList.size()) {
//                this.mMediaPositionsize = j;
//                this.mMediaPlayerControl.selectDuration(j);
//                this.mBeans = paramList;
//                this.mPathType = paramInt1;
//                this.mMediaDuration = paramInt2;
//                this.mMediaPosition = paramInt2;
//                this.getisVideo = false;
//                return;
//            }
//            float f = j;
//            j = (int) (Float.valueOf(((MediaPlayData.SrcBean) paramList.get(i)).getVideo()).floatValue() * 1000.0F + f);
//            i += 1;
//        }
//    }
//
//    public void setVisibilityListener(VisibilityListener paramVisibilityListener) {
//        this.visibilityListener = paramVisibilityListener;
//    }
//
//    public void setZhibo(boolean paramBoolean) {
//        this.iszhixian = paramBoolean;
//        this.getisVideo = false;
//    }
//
//    public void show() {
//        Log.i("cao", "show==");
//        show(5000);
//    }
//
//    public void videoHide() {
//        Log.e("cao", " videoHide()");
//        if (!this.mShowing) ;
//        while (true) {
//            return;
//            try {
//                setVisibility(8);
//                if (this.visibilityListener != null)
//                    this.visibilityListener.onVisibilityChange(8, true);
//                this.mHandler.removeMessages(2);
//                this.mShowing = false;
//                if (this.mHiddenListener == null)
//                    continue;
//                this.mHiddenListener.onHidden();
//                return;
//            } catch (IllegalArgumentException localIllegalArgumentException) {
//                while (true)
//                    Log.e(AppVideoSurfaceView.TAG, "AppVideoControllerView hide():" + localIllegalArgumentException.toString());
//            }
//        }
//    }
//
//    public void videoShow() {
//        Log.e("cao", "界面显示");
//        try {
//            setVisibility(0);
//            if (this.visibilityListener != null)
//                this.visibilityListener.onVisibilityChange(0, true);
//            this.mHandler.removeMessages(1);
//            this.mShowing = true;
//            if (this.mShownListener != null)
//                this.mShownListener.onShown();
//            return;
//        } catch (IllegalArgumentException localIllegalArgumentException) {
//            while (true)
//                Log.e(AppVideoSurfaceView.TAG, "AppVideoControllerView show():" + localIllegalArgumentException.toString());
//        }
//    }
//
//    public static abstract interface OnHiddenListener {
//        public abstract void onHidden();
//    }
//
//    public static abstract interface OnPreviousNextListenr {
//        public abstract void next();
//
//        public abstract void previous();
//    }
//
//    public static abstract interface OnSeekBarCallback {
//        public abstract void onSeekBarStopTracking(SeekBar paramSeekBar);
//
//        public abstract void onSeekbarStartTracking(SeekBar paramSeekBar);
//    }
//
//    public static abstract interface OnShownListener {
//        public abstract void onShown();
//    }
//
//    public static abstract interface VisibilityListener {
//        public abstract void onVisbleShow();
//
//        public abstract void onVisibilityChange(int paramInt, boolean paramBoolean);
//    }
//}
//
///* Location:           C:\Users\Administrator\Desktop\Smali2Java\jd-gui\classes-dex2jar.jar
// * Qualified Name:     com.tigercxvideo.custom.AppVideoControllerView
// * JD-Core Version:    0.6.0
// */