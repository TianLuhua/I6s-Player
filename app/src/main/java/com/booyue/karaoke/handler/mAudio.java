package com.booyue.karaoke.handler;

/**
 * Created by Administrator on 2017/12/29.16:20
 */

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/*
 * To getaudio or play audio
 * */
public class mAudio {
    private AudioRecord audioRecord;
    private Context context;
    private boolean isRecording = false ;
    private PipedOutputStream outstream ;//利用管道传输数据
    public mAudio(Context context , PipedInputStream instream) throws IOException {
        this.context  = context;
        //初始化管道流 用于向外传输数据
        outstream = new PipedOutputStream();
        outstream.connect(instream);
    }
    public void StartAudioData(){//得到录音数据
        int frequency = 11025;
        @SuppressWarnings("deprecation")
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        int buffersize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                frequency, channelConfiguration, audioEncoding, buffersize);
        byte[]buffer  = new byte[buffersize];
        audioRecord.startRecording();//开始录音
        isRecording = true;
        int bufferReadSize = 1024;
        while (isRecording){
            audioRecord.read(buffer, 0, bufferReadSize);
            try {
                outstream.write(buffer, 0, bufferReadSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void stopRecord(){//停止录音
        isRecording = false;
        audioRecord.stop();
        try {
            outstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
