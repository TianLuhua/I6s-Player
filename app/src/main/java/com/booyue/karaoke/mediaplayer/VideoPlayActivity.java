package com.booyue.karaoke.mediaplayer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.KeyEvent;

import com.booyue.karaoke.R;
import com.booyue.karaoke.activity.BaseActivity;
import com.booyue.karaoke.utils.FileUtils;
import com.booyue.karaoke.utils.LoggerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * 视频播放界面
 *
 * @author wangxinhua
 */
public class VideoPlayActivity extends BaseActivity implements MediaController.MediaPlayerUIListener
        , VitamioVideoView.OnVideoErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {
    /**
     * 视频播放对象
     */
    private VitamioVideoView videoView;
    /**
     * 控制器对象
     */
    private MediaController mMediaController;
    /**
     * 声音控制条
     */
    private long mCurPos;//播放进度
    private boolean isBuffering = false;
    private int position = 0;//播放哪个视频
    //    private AudioRecordHandler audioRecordHandler;
//    private AudioPlayHandler audioPlayHandler;
    private List<String> videoInfoList = new ArrayList<>();
    //    private HeadsetPlugReceiver headsetPlugReceiver;
    //    private MediaRecorder recorder;

    @Override
    protected void initView() {
//        if (!LibsChecker.checkVitamioLibs(this)) {
//            return;
//        }
        setContentView(R.layout.activity_video_play);
        videoView = (VitamioVideoView) findViewById(R.id.videoview);
        mMediaController = new MediaController(this);
        mMediaController.setMediaPlayerUIListener(this);
        videoView.setMediaController(mMediaController);
    }

    @Override
    protected void initData() {
//        registerHeadsetPlugReceiver();
        getDataFromActivity();
        initListener();
    }

    protected void initListener() {
        videoView.setOnVideoErrorListener(this);//视频错误监听
        videoView.setOnInfoListener(this);//缓冲监听
        videoView.setOnPreparedListener(this);//加载视频准备监听
        videoView.setOnCompletionListener(this);//播放完成监听
    }


    private void getDataFromActivity() {
        Uri uri = getIntent().getData();
        //根据uri获取媒体文件绝对路径
        String path = FileUtils.getFilePathByUri(getApplicationContext(), uri);
        int startIndex = path.lastIndexOf("/");
        File rootFile = new File(path.substring(0, startIndex));
        String rootPath = rootFile.getPath();
        String[] files = rootFile.list();
        for (String s : files) {
            String childPath = rootPath + "/" + s;
            //系统支持：mp4、mkv格式
            if (childPath.endsWith("mp4") || childPath.endsWith("mkv")) {
                videoInfoList.add(childPath);
//                Log.e("tlh", "getDataFromActivity:" + childPath);
            }
        }
        position = videoInfoList.indexOf(path);
//        Log.e("tlh", "getDataFromActivity--position:" + position);
//        Intent i = getIntent();
//        videoInfoList = i.getStringArrayListExtra("videoInfoList");
//        position = i.getIntExtra("position", 0);

    }

    /**
     * 获取下一个视频的pos
     *
     * @param audioPlayNext 是否自动播放下一首
     */
    private void getNextPos(boolean audioPlayNext) {
        //只有播放完成自动播放下一首才会执行单曲循环
        if (MediaController.STATE_PLAY_MODE == MediaController.STATE_PLAY_MODE_SINGLE && audioPlayNext) {//单曲模式
            return;
        }
        position++;
        if (position >= videoInfoList.size()) {
            position = 0;
        }
    }

    /**
     * 获取上一个位置的pos
     */
    private void getPrevPos() {
        position--;
        if (position < 0) {
            position = videoInfoList.size() - 1;
        }
    }

    /**
     * 初始化每个视频的UI
     */
    public void changeSource() {
        /**校验路径*/
        //去掉后缀名
//        String videoName = videoInfoList.get(position).videoName;
        String videoPath = videoInfoList.get(position);
        int startIndex = videoPath.lastIndexOf("/");
        int endIndex = videoPath.lastIndexOf(".");
        String videoName = videoPath.substring(startIndex + 1, endIndex);
////        if (videoName.contains(".")) {
////            int lastIndexOfDot = videoName.lastIndexOf(".");
////            videoView.setName(videoName.substring(0, lastIndexOfDot));
////        }
        videoView.setName(videoName);
        videoView.setVideoPath(videoPath);
        videoView.requestFocus();
        videoView.start();
//        LoggerUtils.d(TAG + "视频名称：" + videoName);
        LoggerUtils.d(TAG + "视频路径：" + videoPath);
        /**modify by : 2017/11/8 17:31 针对于普通视频和kalaoke视频不同来隐藏伴奏视图*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        changeSource();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaController != null) {
            mMediaController.registerReceiver(this.getApplicationContext());
        }
        if (videoView != null && mCurPos != 0) {
            videoView.seekTo(mCurPos);
        }
    }

    /**
     * 在activity销毁的时候注销广播
     */
    @Override
    protected void onPause() {
        if (mMediaController != null) {
            mMediaController.registerReceiver(this.getApplicationContext());
        }
        mMediaController.unregisterReceiver(this.getApplicationContext());

        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
            mCurPos = videoView.getCurrentPosition();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    @Override
    public void onBack() {
        VideoPlayActivity.this.finish();
    }

    @Override
    public void onPrev() {
        getPrevPos();
        changeSource();
    }

    @Override
    public void onNext() {
        getNextPos(false);
        changeSource();
    }

    @Override
    public void onSwitchTrack() {
//        switchTrack();
    }

    @Override
    public void onVideoError() {
        finish();
    }

    @Override
    public boolean onInfo(final MediaPlayer mp, final int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
//            Logger.d(TAG, "缓冲开始");
            if (!isBuffering) {
                isBuffering = true;
            }
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
//            Logger.d(TAG, "缓冲结束");
            if (isBuffering) {
                isBuffering = false;
            }
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //查阅官网文档，设置缓冲和画面应该放在onPrepare之后
        videoView.setBufferSize(1024 * 512);
        mMediaController.setSupportKalaoke();//判断是否支持卡拉ok，并显示或者隐藏麦克风

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        /**否则播放下一首*/
        getNextPos(true);
        changeSource();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void release() {
        LoggerUtils.d(TAG + "release()");
        if (mMediaController != null) {
            mMediaController.release();
            mMediaController = null;
        }
        if (videoView != null) {
            videoView.stopPlayback();
        }

//        if (audioPlayHandler != null) {
//            audioPlayHandler.stop();
//            audioPlayHandler.release();
//            audioPlayHandler = null;
//        }
//        if (audioRecordHandler != null) {
//            audioRecordHandler.stoppRecord();
//            audioRecordHandler.release();
//            audioRecordHandler.deleteLastRecordFile();
//            audioRecordHandler = null;
//        }

    }

}

