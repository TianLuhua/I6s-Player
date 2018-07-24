package com.booyue.karaoke.mediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.booyue.karaoke.R;
import com.booyue.karaoke.bean.VideoInfo;
import com.booyue.karaoke.utils.LoggerUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/4/23.12:20
 * /storage/emulated/0/mtv/wen lee.mkv
 * /storage/emulated/0/mtv/gu tang.mkv
 */

public class VideoPlayerActivity extends AppCompatActivity {
    public static final String TAG = "VideoPlayerActivity";

    private int position;
    private List<VideoInfo> videoInfoList;
    private VideoView videoView;
    private Button btnSelectTrack;
    private boolean isAccompany = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_activity);
        videoView = (VideoView) findViewById(R.id.videoview);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position += 1;
                if(position >= videoInfoList.size()){
                    position = 0;
                }
                videoView.setDataSource(videoInfoList.get(position).videoPath,true);
            }
        });
        btnSelectTrack = (Button) findViewById(R.id.btn_select_track);
        btnSelectTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerUtils.d(TAG + "btnSelectTrack()");
                if (videoView != null) {
                    if(!isAccompany){
                        videoView.selectTrack(2);
                        isAccompany = true;
                    }else {
                        videoView.selectTrack(1);
                        isAccompany = false;
                    }
                    int index = videoView.getTrackIndex();
                    LoggerUtils.d(TAG  + "index = " + index);
                    if (index != -1) {
                    }
                }
            }
        });
        getDataFromActivity();
        videoView.setDataSource(videoInfoList.get(position).videoPath,true);
    }


    private void getDataFromActivity() {
        Intent i = getIntent();
        videoInfoList = i.getParcelableArrayListExtra("videoInfoList");
        position = i.getIntExtra("position", 0);
    }
}
