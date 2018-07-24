//package com.booyue.karaoke.activity;
//
//import android.Manifest;
//import android.content.Intent;
//import android.graphics.Rect;
//import android.media.MediaPlayer;
//import android.media.MediaRecorder;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.Gravity;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.PopupWindow;
//import android.widget.SeekBar;
//import android.widget.TextView;
//
//import com.booyue.karaoke.R;
//import com.booyue.karaoke.bean.Constants;
//import com.booyue.karaoke.utils.Logger;
//import com.booyue.karaoke.utils.TimeUtils;
//
//import java.io.File;
//import java.io.IOException;
//
//public class VideoActivity extends BaseActivity implements View.OnClickListener {
//    private static final int CHANGE_POPUP = 1;
//    private static final long HIDDEN_TIME = 5000;
//    private static final int UPDATE_PROGRESS = 0;
//    private int currentProgress;
//    private String filePath;
//    private ImageView imageView_play;
//    private boolean isAttachedToWindow = false;
//    private boolean isAudio = false;
////    private boolean isKaraoke;
//    private boolean isMtv = false;
//    private MediaPlayer mediaPlayer;
//    private PopupWindow popupWindow;
//    private MediaRecorder recorder;
//    private SeekBar seekBar;
//    private SurfaceHolder surfaceHolder;
//    private SurfaceView sv_video;
//    private TextView textView_duration;
//    private TextView textView_playTime;
//    private String videoName;
//
////    private boolean isPrepareed = false;//是否准备好了，不然有些视频黑屏
//    private Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case UPDATE_PROGRESS: {
//                    if (mediaPlayer != null) {
//                        if (isAttachedToWindow && textView_playTime != null) {
//                            Logger.e("VideoActivity", "isAttachedToWindow");
//                            int currentPlayer = mediaPlayer.getCurrentPosition();
//                            if (currentPlayer > 0) {
//                                int currentPosition = mediaPlayer.getCurrentPosition();
//                                textView_playTime.setText(TimeUtils.formatTime(currentPosition));
//                                int duration = mediaPlayer.getDuration();
//                                int progress = (int) ((currentPosition / duration) * 100.0f);
//                                seekBar.setProgress(progress);
//                            } else {
//                                textView_playTime.setText("00:00:00");
//                                seekBar.setProgress(0);
//                            }
//                            mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
//                            return;
//                        }
//
//                    }
//                }
//                case CHANGE_POPUP: {
//                    hideController();
//                    Logger.e("VideoActivity", "hidden change_popup");
//                    break;
//                }
//            }
//        }
//    };
//    private ImageView img_loop;
//    private File[] files;
//
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Intent i = getIntent();
////        isKaraoke = i.getBooleanExtra("isKaraoke", true);
//        videoName = i.getStringExtra("videoName");
////        if (isKaraoke) {
////            isMtv = i.getBooleanExtra("isMtv", true);
////            if (isMtv) {
////                filePath = Constants.MTV_PATH + File.separator + videoName;
////            } else if (isKaraoke) {
////                filePath = Constants.KARAOKE_PATH + File.separator + videoName;
////            } else {
////                filePath = Constants.VIDEO_PATH + File.separator + videoName;
////            }
////        }
//        isMtv = i.getBooleanExtra("isMtv", true);
//        if (isMtv) {
//            filePath = Constants.MTV_PATH + File.separator + videoName;
//        } else {
//            filePath = Constants.KARAOKE_PATH + File.separator + videoName;
//        }
//
//
//        Logger.e("VideoActivity", "VIDEO_NAME" + videoName);
//        checkPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, new PermissionListener() {
//            @Override
//            public void success() {
//                initMediaPlayer();
//                initController();
//                initializeAudio();
//            }
//
//            @Override
//            public void fail() {
//
//            }
//        });
//        currentPosition();
//
//    }
//
//
//    /**
//     * 录音初始化
//     */
//    private void initializeAudio() {
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
//    }
//
//    /**
//     * 当弹出windows
//     */
//    public void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        isAttachedToWindow = true;
//    }
//
//    protected void onResume() {
//        super.onResume();
//        isAttachedToWindow = true;
//    }
//
//    protected void initView() {
//        super.initView();
//        setContentView(R.layout.activity_video);
//        sv_video = (SurfaceView) findViewById(R.id.sv_video);
//        sv_video.setOnClickListener(this);
//        View view = View.inflate(this, R.layout.popup_nav, null);
//        ImageButton img_back = (ImageButton) view.findViewById(R.id.img_back);
//        img_back.setOnClickListener(this);
//        surfaceHolder = sv_video.getHolder();
//        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
//            public void surfaceDestroyed(SurfaceHolder holder) {
//                if (mediaPlayer != null) {
//                    currentProgress = mediaPlayer.getCurrentPosition();
//                    mediaPlayer.stop();
//                    mediaPlayer.release();
//                    mediaPlayer = null;
//                }
//            }
//
//            public void surfaceCreated(SurfaceHolder holder) {
//                Logger.e("VideoActivity", "surfaceView:" + sv_video + " mediaPlayer:" + mediaPlayer + " holder:" + holder);
//                if (mediaPlayer != null) {
//                    mediaPlayer.setDisplay(surfaceHolder);
//                    mediaPlayer.seekTo(currentProgress);
//                    mediaPlayer.start();
//                } else {
//                    initMediaPlayer();
//                    mediaPlayer.setDisplay(surfaceHolder);
//                    mediaPlayer.seekTo(currentProgress);
//                    mediaPlayer.start();
//                }
//                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
//            }
//
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            }
//        });
//    }
//
//    /**
//     * 初始化视频播放器
//     */
//    private void initMediaPlayer() {
//
//////        File file = new File(Uri.encode(filePath));
////        if (file.exists()) {
////            Logger.e("VideoActivity", "file exist" + filePath);
////        } else {
////            return;
////        }
//        Logger.e("VideoActivity", "file not exist" + filePath);
//        if (mediaPlayer == null) {
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.reset();
//            try {
////                File file = new File(filePath);
////                FileInputStream fis = new FileInputStream(file);
////                mediaPlayer.setDataSource(fis.getFD());
//                mediaPlayer.setDataSource(filePath);
////                mediaPlayer.prepare();
//                mediaPlayer.prepareAsync();
//
//            } catch (IllegalStateException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            public void onPrepared(MediaPlayer mp) {
//                mediaPlayer.start();
//                mediaPlayer.setLooping(false);
//                if (textView_duration != null) {
//                    textView_duration.setText(TimeUtils.formatTime(mp.getDuration()));
//                }
//                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
//            }
//        });
//        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                mp.reset();
////                changeSource(files[index]);
//                return false;
//            }
//        });
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                next(files);
//                changeSource(files[index]);
//
//            }
//        });
//
//    }
//
//    protected void initData() {
//        super.initData();
//    }
//
//    /**
//     * 初始化控制视图
//     */
//    private void initController() {
//        View controllerView;
//        if (isMtv) {
//            controllerView = getLayoutInflater().inflate(R.layout.popup_karaoke, null);
//            ImageView img_accompany = (ImageView) controllerView.findViewById(R.id.img_accompany);
//            img_accompany.setOnClickListener(this);
//            ImageView img_volume_control = (ImageView) controllerView.findViewById(R.id.img_volume_control);
//            img_volume_control.setOnClickListener(this);
//        } else {
//            controllerView = getLayoutInflater().inflate(R.layout.popup_menu, null);
//        }
//        FrameLayout fl_video = (FrameLayout) controllerView.findViewById(R.id.fl_video);
//        fl_video.setOnClickListener(this);
//        ImageView img_pre = (ImageView) controllerView.findViewById(R.id.img_pre);
//        ImageView img_next = (ImageView) controllerView.findViewById(R.id.img_next);
//        img_pre.setOnClickListener(this);
//        img_next.setOnClickListener(this);
//        imageView_play = (ImageView) controllerView.findViewById(R.id.imageView_play);
//        imageView_play.setOnClickListener(this);
//        textView_playTime = (TextView) controllerView.findViewById(R.id.tv_playtime);
//        textView_duration = (TextView) controllerView.findViewById(R.id.tv_totaltime);
//        img_loop = (ImageView) controllerView.findViewById(R.id.img_loop);
//        img_loop.setOnClickListener(this);
//        seekBar = (SeekBar) controllerView.findViewById(R.id.seekbar);
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, HIDDEN_TIME);
//            }
//
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                mHandler.removeMessages(CHANGE_POPUP);
//            }
//
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser) {
//                    int playtime = (mediaPlayer.getDuration() * progress) / 100;
//                    mediaPlayer.seekTo(playtime);
//                }
//            }
//        });
//        initPopupWindow(controllerView);
//
//    }
//
//    /**
//     * 创建popup
//     *
//     * @param controllerView
//     */
//    private void initPopupWindow(View controllerView) {
//        popupWindow = new PopupWindow(controllerView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, false);
//        popupWindow.setTouchable(true);
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setBackgroundDrawable(null);
//    }
//
//    private String path = null;
//    private int index = 0;
//
//    public void currentPosition() {
////        if (isKaraoke) {
////            if (isMtv) {
////                path = Constants.MTV_PATH;
////            } else if (isKaraoke) {
////                path = Constants.KARAOKE_PATH;
////            } else {
////                path = Constants.VIDEO_PATH;
////            }
////        }
//            if (isMtv) {
//                path = Constants.MTV_PATH;
//            } else {
//                path = Constants.KARAOKE_PATH;
//            }
//
//        Logger.e("VideoActivity", "path:" + path);
//        File f_root = new File(path);
//        files = f_root.listFiles();
//        for (int i = 0; i < files.length; i = i + 1) {
//            if (files[i].getName().contains(videoName)) {
//                index = i;
//            }
//        }
//    }
//
//    /**
//     * 上一首
//     *
//     * @param files
//     */
//    public void prev(File[] files) {
//        if (index == 0) {
//            index = (files.length - 1);
//        } else {
//            index = (index - 1);
//        }
//    }
//
//    /**
//     * 下一首
//     *
//     * @param files
//     */
//    public void next(File[] files) {
//        if (index == (files.length - 1)) {
//            index = 0;
//        } else {
//            index = (index + 1);
//        }
//    }
//
//    /**
//     * 更改播放源
//     *
//     * @param f
//     */
//    private void changeSource(File f) {
//        mediaPlayer.stop();
//        mediaPlayer.reset();
//        try {
//            mediaPlayer.setDataSource(f.getPath());
//            mediaPlayer.prepareAsync();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * 隐藏控制视图
//     */
//    private void hideController() {
//        if (isAttachedToWindow && popupWindow != null) {
//            if (popupWindow.isShowing()) {
//                popupWindow.dismiss();
//            }
//        }
//    }
//
//    /**
//     * 显示控制视图
//     */
//    private void showController() {
//        Logger.e("VideoActivity", "popup show");
//        int[] location = new int[2];
//        sv_video.getLocationOnScreen(location);
//        Rect anchorRect = new Rect(location[0], location[1], location[0] + sv_video.getWidth(), location[1] + sv_video.getHeight());
//        if (Build.VERSION.SDK_INT == 24) {//由于7.0似乎不支持Gravity.NO_GRAVITY属性
//            popupWindow.showAtLocation(sv_video, Gravity.TOP, 0, 0);
//        } else {
//            popupWindow.showAtLocation(sv_video, Gravity.NO_GRAVITY, anchorRect.left, anchorRect.bottom);
//        }
////      popupWindow.showAtLocation(sv_video, Gravity.NO_GRAVITY, 0, 0);
//        mHandler.removeMessages(CHANGE_POPUP);
//        mHandler.sendEmptyMessageDelayed(CHANGE_POPUP, HIDDEN_TIME);
//    }
//
//    /**
//     * 显示/隐藏控制视图
//     */
//
//    private void showOrHiddenController() {
//        if (popupWindow == null) return;
//        if (isAttachedToWindow) {
//            boolean showing = popupWindow.isShowing();
//            Logger.e("VideoActivity", "popupWindow state = " + showing);
//            if (showing) {
//                Logger.e("VideoActivity", "popupWindow dismiss");
//                popupWindow.dismiss();
//                return;
//            } else {
//                Logger.e("VideoActivity", "popupWindow create");
//                showController();
//            }
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        int id = v.getId();
//        switch (id) {
//            case R.id.sv_video: {//视频控件surfaceview
//                Logger.e("VideoActivity", "sv_video");
//                showOrHiddenController();
//                return;
//            }
//            case R.id.img_back: {//回退键
//                finish();
//                break;
//            }
//            case R.id.imageView_play://播放、暂停
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    imageView_play.setImageResource(R.drawable.play);
//                    return;
//                } else {
//                    mediaPlayer.start();
//                    imageView_play.setImageResource(R.drawable.pause);
//                }
//                break;
//            case R.id.img_loop:
//                Logger.e("VideoActivity", "isLooping:" + mediaPlayer.isLooping());
//                if (mediaPlayer.isLooping()) {
//                    mediaPlayer.setLooping(false);
//                    img_loop.setImageResource(R.drawable.loop_once);
//                    return;
//                } else {
//                    mediaPlayer.setLooping(true);
//                    img_loop.setImageResource(R.drawable.loop);
//
//                }
//                break;
//            case R.id.img_accompany:
//                Logger.e(TAG, "img_accompany");
//                switchTrack();
//                break;
//            case R.id.img_volume_control:
//
//                break;
//            case R.id.fl_video:
//                showOrHiddenController();
//                break;
//            case R.id.img_pre:
//                if (files != null) {
//                    prev(files);
//                    File file = files[index];
//                    changeSource(file);
//                }
//                break;
//            case R.id.img_next:
//                if (files != null) {
//                    next(files);
//                    File file = files[index];
//                    changeSource(file);
//                }
//                break;
//        }
//    }
//
//    /**
//     * 切换声道
//     */
//    private void switchTrack() {
//        MediaPlayer.TrackInfo[] infoarr = mediaPlayer.getTrackInfo();
//        Logger.v("VideoActivity", "infoarr=" + infoarr.length);
//        if (infoarr.length > 0) {
//            for (MediaPlayer.TrackInfo trackInfo : infoarr) {
//                Logger.v("VideoActivity", "TrackInfo=" + trackInfo.getTrackType());
//                Logger.e("VideoActivity", "isAudio:" + isAudio);
//                if (!isAudio && trackInfo.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
//                    mediaPlayer.selectTrack(trackInfo.getTrackType());//伴奏开启，主唱关闭
//                    isAudio = true;
//                    return;
//                } else if (isAudio && trackInfo.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
//                    mediaPlayer.selectTrack(trackInfo.getTrackType());//伴唱关闭，原唱开启
//                    isAudio = false;
//                    return;
//                }
//            }
//        }
//    }
//
//    protected void onPause() {
//        super.onPause();
//        isAttachedToWindow = false;
//    }
//
//
//    protected void onDestroy() {
//        if (popupWindow != null && popupWindow.isShowing()) {
//            popupWindow.dismiss();
//            popupWindow = null;
//        }
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//        if (recorder != null) {
//            recorder.stop();
//            recorder.release();
//        }
//        mHandler.removeCallbacksAndMessages(null);
//        mHandler = null;
//        super.onDestroy();
//    }
//}
