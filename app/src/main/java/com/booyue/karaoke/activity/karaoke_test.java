//package com.booyue.karaoke.activity;
//
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.Button;
//
//import com.booyue.karaoke.R;
//import com.booyue.karaoke.handler.AudioPlayHandler;
//import com.booyue.karaoke.handler.AudioRecordHandler;
//import com.booyue.karaoke.utils.LoggerUtils;
//
//public class karaoke_test extends AppCompatActivity {
//    private static final String TAG = "karaoke_test";
////
////    PipedInputStream in;
////    boolean isRrcord;
////    mAudio mm;
////    mAudioPlayer m;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_karaoke_test);
//        final Button btnClick = (Button) findViewById(R.id.btn_click);
//        btnClick.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                btnClick();
//                initializeAudio();
//            }
//        });
//
////        isRrcord = false;
//
//    }
//    private AudioRecordHandler audioRecordHandler;
//    private AudioPlayHandler audioPlayHandler;
//    private void initializeAudio() {
////        MediaPlayer mediaPlayer = new MediaPlayer(this);
//
//        audioRecordHandler = new AudioRecordHandler();
//        audioPlayHandler = new AudioPlayHandler();
//        audioPlayHandler.prepare();
//        audioRecordHandler.startRecord(new AudioRecordHandler.AudioRecordingCallback() {
//            @Override
//            public void onRecording(byte[] data, int startIndex, int length) {
//                LoggerUtils.d(TAG + "onRecording(byte[] data, int startIndex, int length):data = " + data.toString());
////                audioPlayHandler.onRecordDataCallback(data,startIndex,length);
//            }
//
//            @Override
//            public void onStopRecord(String savedPath) {
//                audioRecordHandler.deleteLastRecordFile();
//
//            }
//        });
//    }
//
////    public void btnClick() {
////        if (isRrcord) {
////            isRrcord = false;
////            mm.stopRecord();
////            m.stopPlay();
////        } else {
////            isRrcord = true;
////            startRecord();
////        }
////    }
////
////    private void startRecord() {
////        in = new PipedInputStream();
////        new Thread(new Runnable() {
////
////            @Override
////            public void run() {
////                try {
////                    mm = new mAudio(karaoke_test.this, in);
////                    mm.StartAudioData();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////        }).start();
////        new Thread(new Runnable() {
////            @Override
////            public void run() {
////                byte[] buffer = new byte[1024];
////                PipedOutputStream pout = new PipedOutputStream();
////                m = new mAudioPlayer();
////                try {
////                    m.setOutputStream(pout);
////                    new Thread(new Runnable() {
////
////                        @Override
////                        public void run() {
////                            // TODO Auto-generated method stub
////                            m.startPlayAudio();
////                        }
////                    }).start();
////                } catch (IOException e1) {
////                    e1.printStackTrace();
////                }
////                int size = 0;
////                try {
////                    while (true) {
////                        while (in.available() > 0) {
////                            size = in.read(buffer);
////                            pout.write(buffer, 0, size);
////                        }
////                    }
////
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////        }).start();
////    }
//}
