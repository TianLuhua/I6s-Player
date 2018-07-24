package com.booyue.karaoke.mediaplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.booyue.karaoke.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/23.11:21
 */

public class VideoView extends SurfaceView {
    private static final int CHANGE_MODE_STPO = 4;
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PREPARING = 1;
    public static final String TAG = VideoView.class.getSimpleName();
    private volatile boolean mAsyncStopping;
    private boolean mAutoDestroySurfaceHolder = true;
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer paramMediaPlayer, int paramInt) {
            mCurrentBufferPercentage = paramInt;
        }
    };
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer paramMediaPlayer) {
//            Log.i(VideoView.TAG, "OnCompletionListener");
            mCurrentState = 5;
            mTargetState = 5;
//            if (mVideoControllerView != null)
//                mVideoControllerView.hide();
//            int i;
//            if ((mOnCompletionListeners != null) && (!mOnCompletionListeners.isEmpty()))
//                i = 0;
//            while (true) {
//                if (i >= mOnCompletionListeners.size())
//                    return;
//                ((MediaPlayer.OnCompletionListener) mOnCompletionListeners.get(i)).onCompletion(mMediaPlayer);
//                i += 1;
//            }
        }
    };
    private Context mContext;
    private int mCurrentBufferPercentage;
    private int mCurrentPosition;
    private volatile int mCurrentState = 0;
    private int mDuration = -1;
    //    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
//        public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
//            Log.d(VideoView.TAG, "onError: " + paramInt1 + "," + paramInt2);
//            int i;
//            if (paramInt2 == -2147483648) {
//                showToast(mContext, "未指明的低级系统错误");
//                mCurrentState = -1;
//                mTargetState = -1;
//                if (mVideoControllerView != null)
//                    mVideoControllerView.hide();
//                if ((mOnErrorListeners != null) && (!mOnErrorListeners.isEmpty()))
//                    i = 0;
//            }
//            while (true) {
//                if (i >= mOnErrorListeners.size()) {
//                    return true;
//                    if (paramInt2 == -1010) {
//                        showToast(mContext, "不支持该格式的视频播放");
//                        break;
//                    }
//                    if (paramInt2 == -110) {
//                        showToast(mContext, "播放超时，请稍后重试");
//                        break;
//                    }
//                    showToast(mContext, "视频播放出错，请稍后重试");
//                    break;
//                }
//                ((MediaPlayer.OnErrorListener) mOnErrorListeners.get(i)).onError(mMediaPlayer, paramInt1, paramInt2);
//                i += 1;
//            }
//        }
//    };
//    private final Handler mHandler = new Handler() {
//        public void handleMessage(Message paramMessage) {
//            switch (paramMessage.what) {
//                default:
//                case 4:
//            }
//            do
//                return;
//            while (paramMessage == null);
//            if ((mMediaPlayer != null) && (mMediaPlayer.isPlaying())) {
//                if (mCurrentPosition != getCurrentPosition())
//                    break label115;
//                mVideoControllerView.videoShow();
//                mIsVideokey = true;
//            }
//            while (true) {
//                mCurrentPosition = getCurrentPosition();
//                sendMessageDelayed(obtainMessage(4), 2500L);
//                return;
//                label115:
//                if (!mIsVideokey)
//                    continue;
//                mIsVideokey = false;
//                mVideoControllerView.videoHide();
//            }
//        }
//    };
    private boolean mIsVideokey = false;
    private MediaPlayer mMediaPlayer = null;
    private MediaPlayer mMediaPlayer2 = null;
    private List<MediaPlayer.OnCompletionListener> mOnCompletionListeners = new ArrayList();
    private List<MediaPlayer.OnErrorListener> mOnErrorListeners = new ArrayList();
    private List<MediaPlayer.OnPreparedListener> mOnPreparedListeners = new ArrayList();
    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private int mPathCurrent = 0;
    private boolean mPathPosition = true;
    private boolean mPathlist = false;
    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer paramMediaPlayer) {
            mCurrentState = 2;
            int i;
            mVideoWidth = paramMediaPlayer.getVideoWidth();
            mVideoHeight = paramMediaPlayer.getVideoHeight();
            if ((mVideoWidth != 0) && (mVideoHeight != 0)) {
                play();
//                    toggleMediaControlsVisiblity();

                if (mOnPreparedListeners != null && mOnPreparedListeners.size() > 0) {
                    for (MediaPlayer.OnPreparedListener onPreparedListener : mOnPreparedListeners) {
                        onPreparedListener.onPrepared(paramMediaPlayer);
                    }
                }
            }

        }

    };
    private long mPressDownTime;
    private SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int format, int w, int h) {
//            Log.d(VideoView.TAG, "surfaceChanged:w:" + w + ";h:" + h);
//            if (mTargetState == 3) {
//                 = 1;
//                if ((mVideoWidth != paramInt2) || (mVideoHeight != paramInt3)){
//                    if ((mMediaPlayer != null) && (paramInt1 != 0) && (paramInt2 != 0)) {
//                        if (mSeekWhenPrepared != 0)
//                            seekTo(mSeekWhenPrepared);
//                        play();
//                    }
//                }
//            }
            mVideoHeight = w;
            mVideoHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0)
                    seekTo(mSeekWhenPrepared);
                play();
//                if (mMediaController != null) {
//                    if (mMediaController.isShowing())
//                        mMediaController.hide();
//                    mMediaController.show();
//                }
            }

        }

        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Canvas canvas = null;
            mSurfaceHolder = surfaceHolder;
            if (!isSurfaceValid(mSurfaceHolder)) return;
            if (mAutoDestroySurfaceHolder) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(mSurfaceHolder);
                    openVideo();
                } else {
                    openVideo();
                }
//                while (true) {
//                    if (mMediaPlayer == null) continue;
//                    canvas = surfaceHolder.lockCanvas();
//                    canvas.drawColor(getResources().getColor(android.R.color.black));
//                }
//                if (canvas != null) {
//                    mSurfaceHolder.unlockCanvasAndPost(canvas);
//                }
            }
        }

        public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
//            Log.d(VideoView.TAG, "surfaceDestroyed");
            mSurfaceHolder = null;
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    };
    private int mSeekWhenPrepared;
    private MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
//            Log.d(VideoView.TAG, "onVideoSizeChanged: width:" + paramInt1 + ";height" + paramInt2);
            mVideoWidth = paramMediaPlayer.getVideoWidth();
            mVideoHeight = paramMediaPlayer.getVideoHeight();
//            Log.d(VideoView.TAG, "onVideoSizeChanged: mVideoWidth:" + mVideoWidth + ";mVideoHeight" + mVideoHeight);
            if (mOnVideoSizeChangedListener != null)
                mOnVideoSizeChangedListener.onVideoSizeChanged(paramMediaPlayer, paramInt1, paramInt2);
            if ((mVideoWidth != 0) && (mVideoHeight != 0) && (getHolder() != null))
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            requestLayout();//自己添加，查看系统videoview，存在这句话
        }
    };
    private SurfaceHolder mSurfaceHolder = null;
    private volatile int mTargetState = 0;
    private Uri mUri;
    private String mUrl;
    //    private AppVideoControllerView mVideoControllerView;
    private int mVideoHeight;
    private int mVideoWidth;
    private int selectIndex = 0;

    public VideoView(Context paramContext) {
        this(paramContext, null);
    }

    public VideoView(Context paramContext, AttributeSet paramAttributeSet) {
        this(paramContext, paramAttributeSet, 0);
    }

    public VideoView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        this.mContext = paramContext;
        initVideoView();
    }

//    private void attachMediaController() {
//        if (mVideoControllerView != null)
//            this.mVideoControllerView.setMediaPlayerControl(this);
//    }

    private void initVideoView() {
        this.mVideoWidth = 0;
        this.mVideoHeight = 0;
        this.mSurfaceHolder = getHolder();
        Log.d(TAG, "initVideoView:mSurfaceHolder:" + this.mSurfaceHolder);
        this.mSurfaceHolder.addCallback(this.mSHCallback);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        this.mCurrentState = 0;
        this.mTargetState = 0;
    }

    //是否在播放状态
    private boolean isInPlaybackState() {
        return (this.mMediaPlayer != null) && (this.mCurrentState != -1) &&
                (this.mCurrentState != 0) && (this.mCurrentState != 1) && (!this.mAsyncStopping);
    }

    private boolean isPlayingSafelyCheck(MediaPlayer paramMediaPlayer) {
        boolean bool = paramMediaPlayer.isPlaying();
        return bool;
    }

    //判断是否有效
    private boolean isSurfaceValid(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null)
            return false;
        if ((surfaceHolder.getSurface() == null) || (!surfaceHolder.getSurface().isValid())) {
            return false;
        }
        return true;
    }

    private void openVideo() {
        if ((this.mUri == null) || (this.mSurfaceHolder == null) || (this.mContext == null))
            return;
        try {
            release(false);
            this.mMediaPlayer = new MediaPlayer();
            this.mMediaPlayer.setDataSource(this.mUrl);
            this.mMediaPlayer.setOnPreparedListener(this.mPreparedListener);
            this.mMediaPlayer.setOnVideoSizeChangedListener(this.mSizeChangedListener);
            this.mMediaPlayer.setOnCompletionListener(this.mCompletionListener);
//            this.mMediaPlayer.setOnErrorListener(this.mErrorListener);
            this.mMediaPlayer.setOnBufferingUpdateListener(this.mBufferingUpdateListener);
            this.mMediaPlayer.setDisplay(this.mSurfaceHolder);
            this.mMediaPlayer.setScreenOnWhilePlaying(true);
            this.mMediaPlayer.prepareAsync();
            this.mCurrentState = 1;
//            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), 1000L);
            return;
        } catch (Exception localException) {
            this.mCurrentState = -1;
            this.mTargetState = -1;
//            this.mErrorListener.onError(this.mMediaPlayer, 1, 0);
        }
    }

    private void release(boolean paramBoolean) {
        if (this.mMediaPlayer != null) ;
        try {
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mAsyncStopping = false;
            this.mCurrentState = 0;
            if (paramBoolean)
                this.mTargetState = 0;
            return;
        } catch (Exception localException) {
            localException.printStackTrace();
        } finally {
            this.mMediaPlayer = null;
            this.mAsyncStopping = false;
        }

    }

//    private void toggleMediaControlsVisiblity() {
//        Log.i("cao", "mVideoControllerView.isShowing()" + this.mVideoControllerView.isShowing());
//        if ((isInPlaybackState()) && (this.mVideoControllerView != null)) {
//            if ((this.mVideoControllerView.isShowing()) && (VideoFragment.TopShow))
//                this.mVideoControllerView.hide();
//        } else
//            return;
//        this.mVideoControllerView.show();
//        VideoFragment.TopShow = true;
//    }

    public void addOnCompletionListener(MediaPlayer.OnCompletionListener paramOnCompletionListener) {
        if (!this.mOnCompletionListeners.contains(paramOnCompletionListener))
            this.mOnCompletionListeners.add(paramOnCompletionListener);
    }

    public void addOnErrorListener(MediaPlayer.OnErrorListener paramOnErrorListener) {
        if (!this.mOnErrorListeners.contains(paramOnErrorListener))
            this.mOnErrorListeners.add(paramOnErrorListener);
    }

    public void addOnPreparedListener(MediaPlayer.OnPreparedListener paramOnPreparedListener) {
        if (!this.mOnPreparedListeners.contains(paramOnPreparedListener))
            this.mOnPreparedListeners.add(paramOnPreparedListener);
    }

    public void completionToNext() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mCompletionListener.onCompletion(this.mMediaPlayer);
        }
    }

    public int getBufferPercentage() {
        if (this.mMediaPlayer != null)
            return this.mCurrentBufferPercentage;
        return 0;
    }

    public int getCurrentPosition() {
        try {
            if (isInPlaybackState()) {
                int i = this.mMediaPlayer.getCurrentPosition();
                return i;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return 0;
    }

    public long getDuration() {
        try {
            if (isInPlaybackState()) {
                if (this.mDuration > 0)
                    return this.mDuration;
                Log.i("zhang", "isInPlaybackState =11=" + this.mMediaPlayer.getDuration());
                int i = this.mMediaPlayer.getDuration();
                return i;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            this.mDuration = -1;
        }
        return this.mDuration;
    }

    public boolean getisMediaPlayertype() {
        return this.mPathlist;
    }

    public long getmDuration() {
        try {
            if (isInPlaybackState()) {
                Log.i("zhang", "isInPlaybackState =55=" + this.mMediaPlayer.getDuration());
                int i = this.mMediaPlayer.getDuration();
                return i;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            this.mDuration = -1;
        }
        return this.mDuration;
    }

    public int getselectIndex() {
        return this.selectIndex;
    }

    public boolean isPaused() {
        return (this.mMediaPlayer != null) && (this.mCurrentState == 4);
    }

    public boolean isPlayCompleted() {
        return this.mCurrentState == 5;
    }

    public boolean isPlaying() {
        if (isInPlaybackState()) {
            return this.mMediaPlayer.isPlaying();
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        long l = paramMotionEvent.getDownTime();
        if ((paramMotionEvent.getAction() == 0) && (this.mPressDownTime != l)) {
            this.mPressDownTime = l;
//            toggleMediaControlsVisiblity();
        }
        return false;
    }

    public boolean onTrackballEvent(MotionEvent paramMotionEvent) {
//        toggleMediaControlsVisiblity();
        return false;
    }

    public void pause() {
        Log.d(TAG, "pause");
        try {
            if ((isInPlaybackState()) && (isPlayingSafelyCheck(this.mMediaPlayer))) {
                this.mMediaPlayer.pause();
                this.mCurrentState = 4;
            }
            this.mTargetState = 4;
            return;
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void play() {
//        Log.d("cao", "play");
        try {
            if ((isInPlaybackState()) && (!isPlayingSafelyCheck(this.mMediaPlayer))) {
                this.mMediaPlayer.start();
                this.mCurrentState = 3;
            }
            this.mTargetState = 3;
            return;
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void seekTo(int paramInt) {
        try {
            if (isInPlaybackState()) {
                Log.i("zhang", "isInPlaybackState ==" + paramInt);
                this.mMediaPlayer.seekTo(paramInt);
                this.mSeekWhenPrepared = 0;
                return;
            }
            this.mSeekWhenPrepared = paramInt;
            return;
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void selectCurrentPosition(int paramInt) {
        this.mPathCurrent += paramInt;
    }

    public void selectDuration(int paramInt) {
        this.mDuration = paramInt;
    }

    public void selectIndex(int paramInt) {
        this.selectIndex = paramInt;
    }

    private boolean mAccompany = false;

    public void selectTrack(int paramInt) {
        if (isInPlaybackState()) {
            LoggerUtils.d("showPrigAccom  selectTrack==" + paramInt);
            this.mMediaPlayer.selectTrack(paramInt);
        }
        return;
    }


    //获取需要切换的音轨索引
    public int getTrackIndex() {
        if (mMediaPlayer != null) {
            MediaPlayer.TrackInfo[] trackInfos = mMediaPlayer.getTrackInfo();
            for (MediaPlayer.TrackInfo trackInfo : trackInfos) {
                LoggerUtils.d("tracktype = " + trackInfo.getTrackType() + "\n");
                if (trackInfo.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                    LoggerUtils.d("trackinfo = " + trackInfo.toString());
                }
            }
//            if (trackInfos != null && trackInfos.length > 0) {
//                for (int i = 0; i < trackInfos.length; i++) {
//                    if (trackInfos[i].getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
//                        return i;
//                    }
//                }
//            }
        }
        return -1;
    }

    public void setAutoDestroySurfaceHolder(boolean paramBoolean) {
        this.mAutoDestroySurfaceHolder = paramBoolean;
    }

    public void setDataSource(String paramString, boolean paramBoolean) {
        this.mUrl = paramString;
        this.mPathlist = paramBoolean;
        this.mUri = Uri.parse(paramString);
        this.mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }
}
