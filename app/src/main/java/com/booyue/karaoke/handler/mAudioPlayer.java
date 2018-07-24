package com.booyue.karaoke.handler;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by Administrator on 2017/12/29.16:21
 */

public class mAudioPlayer {
    private PipedInputStream instream;
    private boolean isPlaying ;
    private AudioTrack audioplayer;
    private byte[] buffer;
    public mAudioPlayer() {
        isPlaying = false;
        instream = null;
        //初始化播音类
        @SuppressWarnings("deprecation")
        int bufsize = AudioTrack.getMinBufferSize(11025, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioplayer = new AudioTrack(AudioManager.STREAM_MUSIC, 11025, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, bufsize,AudioTrack.MODE_STREAM);
    }
    //设置管道流，用于接受音频数据
    public void setOutputStream(PipedOutputStream out) throws IOException {
        instream = new PipedInputStream(out);

    }
    public void startPlayAudio(){ //调用之前先调用setOutputStream 函数
        isPlaying = true;
        audioplayer.play();//开始接受数据流播放
        buffer = new byte[1024];
        while (instream!=null&&isPlaying){
            try {
                while (instream.available()>0){
                    int size = instream.read(buffer);
                    audioplayer.write(buffer, 0
                            , size);//不断播放数据
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void stopPlay(){//停止播放
        isPlaying = false ;
        try {
            instream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioplayer.stop();
    }
}
