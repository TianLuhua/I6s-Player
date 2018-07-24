package io.vov.vitamio.handler;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

/**
 * Created by Administrator on 2017/11/9.15:55
 */

/**
 * 实时音频录制处理类<br/>
 * 记得申明系统权限：MODIFY_AUDIO_SETTINGS、RECORD_AUDIO<br/>
 * 使用实例代码：<br/>
 *
 * <pre>
 * audioRecoderHandler = new AudioRecoderHandler(this);
 * audioRecoderHandler.startRecord(new AudioRecordingCallback() {
 *  &#064;Override
 *  public void onStopRecord(String savedPath) {
 *
 *  }
 *
 *  &#064;Override
 *  public void onRecording(byte[] data, int startIndex, int length) {
 *      // TODO 录制监听。处理data即可。立即播放or发送出去，随你。
 *  }
 * });
 * </pre>
 *
 * @author 李长军
 *
 */
@SuppressWarnings("deprecation")
public class AudioPlayHandler implements Runnable {

    private AudioTrack track = null;// 录音文件播放对象
    private boolean isPlaying = false;// 标记是否正在录音中
    private int frequence = 8000;// 采样率 8000
    private int channelInConfig = AudioFormat.CHANNEL_OUT_MONO;// 定义采样为双声道（过时，但是使用其他的又不行
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;// 定义音频编码（16位）
    private int bufferSize = -1;// 播放缓冲大小
    private LinkedBlockingDeque<Object> dataQueue = new LinkedBlockingDeque<>();
    // 互斥信号量
    private Semaphore semaphore = new Semaphore(1);
    // 是否释放资源的标志位
    private boolean release = false;

    public AudioPlayHandler() {
        // 获取缓冲 大小
        bufferSize = AudioTrack.getMinBufferSize(frequence, channelInConfig, audioEncoding);
        // 实例AudioTrack
        track = new AudioTrack(
                AudioManager.STREAM_MUSIC, //声音类型
                frequence,
                channelInConfig,
                audioEncoding,
                bufferSize,
                AudioTrack.MODE_STREAM);//模式 MODE_STREAM MODE_STATIC
        float minVolume = AudioTrack.getMinVolume();
        float maxVolume = AudioTrack.getMaxVolume();
        float volume = (minVolume + maxVolume) / 2;
        track.setStereoVolume(volume, volume);
//        track.setStereoVolume()
        try {
            // 默认需要抢占一个信号量。防止播放进程执行
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 开启播放线程
        new Thread(this).start();
    }

    /**
     * 播放，当有新数据传入时，
     * @param data 语音byte数组
     * @param startIndex 开始的偏移量
     * @param length  数据长度
     *
     */
    public synchronized void onRecordDataCallback(byte[] data, int startIndex, int length) {
        if (AudioTrack.ERROR_BAD_VALUE == bufferSize) {// 初始化错误
            return;
        }
        try {
            dataQueue.putLast(data);//播放缓冲去有数据
//            semaphore.acquire();//获取锁，防止执行播放任务
            semaphore.release();//释放锁，如果缓冲队列有数据时，释放锁，让播放器质性播放，
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 准备播放
     */
    public void prepare() {
        if (track != null && !isPlaying) {
            track.play();
            isPlaying = true;
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (track != null) {
            track.stop();
            isPlaying = false;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        release = true;
        semaphore.release();
        if (track != null) {
            track.release();
            track = null;
        }
    }

    @Override
    public void run() {
        while (true) {
            if (release) {
                return;
            }
            if (dataQueue.size() > 0) {
                byte[] data = (byte[]) dataQueue.pollFirst();
                track.write(data, 0, data.length);
            } else {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
