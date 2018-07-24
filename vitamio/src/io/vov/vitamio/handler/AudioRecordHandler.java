package io.vov.vitamio.handler;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.io.File;

/**
 * Created by Administrator on 2017/11/9.15:54
 */
@SuppressWarnings("deprecation")
public class AudioRecordHandler {
    /**
     * @param recording resource 录音源 See {@link MediaRecorder.AudioSource}
     * @param sampleRate 采样率
     * @param channelConfig 通道配置
     * @param audioFormat 音频格式
     * @param bufferSizeInBytes 缓冲大小
     */
    //录音数据单次回调数组最大为多少
    private static int MAX_DATA_LENGTH = 160;

    private AudioRecord audioRecord;//录音对象
    private int audioResource = MediaRecorder.AudioSource.MIC;
    private int sampleRateInHz = 8000;//采样率
    private int channelInConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;//定义采样通道
    private int audioEncodeing = AudioFormat.ENCODING_PCM_16BIT;//定义音频编码
    private byte[] buffer = null;// 录制的缓冲数组
    private boolean isRecording = false;//是否正在录音中
    private File lastCacheFile = null;// 记录上次录制的文件名
//    private Context context;

    public AudioRecordHandler() {
//        if(context == null){
//            throw new RuntimeException("context could not be null");
//        }
//        this.context = context;
    }

    /**
     * 开始录制音频
     *
     * @param audioRecordingCallback 录制过程中的回调函数
     */
    public void startRecord(AudioRecordingCallback audioRecordingCallback) {
        RecordTask task = new RecordTask(audioRecordingCallback);
        task.execute();// 开始执行
    }

    /**
     * 删除上次录制的文件（一般是用户取消发送导致删除上次录制的内容）
     *
     * @return true表示删除成功，false表示删除失败，一般是没有上次录制的文件，或者文件已经被删除了
     */
    public boolean deleteLastRecordFile() {
        boolean success = false;
        if (lastCacheFile != null && lastCacheFile.exists()) {
            success = lastCacheFile.delete();
        }
        return success;
    }

    /**
     * 录制音频的任务类
     *
     * @author 李长军
     */
    private class RecordTask extends AsyncTask<String, Integer, String> {

        private AudioRecordingCallback audioRecordingCallback = null;

        public RecordTask(AudioRecordingCallback audioRecordingCallback) {
            this.audioRecordingCallback = audioRecordingCallback;
        }

        @Override
        protected void onPreExecute() {
            // 根据定义好的几个配置，来获取合适的缓冲大小
            // int bufferSize = 800;
            int bufferSize = AudioRecord.getMinBufferSize(
                    sampleRateInHz,
                    channelInConfig,
                    audioEncodeing);
            // 实例化AudioRecord
            audioRecord = new AudioRecord(
                    audioResource,
                    sampleRateInHz,
                    channelInConfig,
                    audioEncodeing,
                    bufferSize);//bufferSize这个参数一般是最小缓冲区大小的整数倍
            // 定义缓冲数组
            buffer = new byte[bufferSize];
            MAX_DATA_LENGTH = bufferSize >>> 1;//无符号右移，相当于除以 2，不过>>>性能稍微要好
            audioRecord.startRecording();// 开始录制
            isRecording = true;// 设置录制标记为true
        }

        @Override
        protected void onPostExecute(String result) {
            audioRecord = null;
            if (result == null) {
                lastCacheFile = null;
            } else {
                lastCacheFile = new File(result);
            }
            if (audioRecordingCallback != null) {
                audioRecordingCallback.onStopRecord(result);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String tempFileName = null;
            // 开始录制
            while (isRecording) {
                // 录制的内容放置到了buffer中，result代表存储长度
                int result = audioRecord.read(buffer, 0, buffer.length);
                // 将数据回调出去
                if (audioRecordingCallback != null) {
                    int offset = result % MAX_DATA_LENGTH > 0 ? 1 : 0;
                    for (int i = 0; i < result / MAX_DATA_LENGTH + offset; i++) {
                        int length = MAX_DATA_LENGTH;
                        if ((i + 1) * MAX_DATA_LENGTH > result) {
                            length = result - i * MAX_DATA_LENGTH;
                        }
                        audioRecordingCallback.onRecording(buffer, i * MAX_DATA_LENGTH, length);
                    }
                }
            }
            return tempFileName;
        }
    }


    /**
     * 监听录制过程，用于实时获取录音数据
     *
     * @author 李长军
     */
    public interface AudioRecordingCallback {
        /**
         * 录音数据获取回调
         *
         * @param data       数据数组对象
         * @param startIndex 数据其开始
         * @param length     数据的结尾
         */
        public void onRecording(byte[] data, int startIndex, int length);

        /**
         * 录音结束后的回调
         *
         * @param savedPath 录音文件存储的路径
         */
        public void onStopRecord(String savedPath);
    }

    /**
     * 停止录音数据回调
     * 不需要录音播放时调用
     */
    public void stopRecordCallback() {
        isRecording = false;
    }
    /**
     * 释放资源
     */
    public void release() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

    }
}
