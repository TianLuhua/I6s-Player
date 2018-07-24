package com.booyue.karaoke.activity;

import android.Manifest;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.booyue.karaoke.R;
import com.booyue.karaoke.adapter.MyGridViewAdapter;
import com.booyue.karaoke.bean.Constants;
import com.booyue.karaoke.bean.VideoInfo;
import com.booyue.karaoke.utils.Logger;
import com.booyue.karaoke.utils.LoggerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 视频列表页面
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private String[] filePath = new String[2];
    private GridView gv_video;
    private FrameLayout img_back;
    private TextView tv_title;
    private ArrayList<VideoInfo> videoInfoArrayList;

    protected void initView() {
        super.initView();
        setContentView(R.layout.activity_main);
        tv_title = getViewById(R.id.tv_title);
        gv_video = getViewById(R.id.gv_video);
        img_back = getViewById(R.id.img_back);
        img_back.setOnClickListener(this);
    }

    protected void initData() {
        Intent i = getIntent();
        boolean isMtv = i.getBooleanExtra("isMtv", true);
        if (isMtv) {
            filePath[0] = Constants.MTV_PATH;
            filePath[1] = Constants.MTV_PATH_YIHENGKE;
            tv_title.setText(R.string.mtv);
        } else {
            filePath[0] = Constants.KARAOKE_PATH;
            filePath[1] = Constants.KARAOKE_PATH_YIHENGKE;
            tv_title.setText(R.string.app_name);
        }
        videoInfoArrayList = new ArrayList();
        checkPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionListener() {
            @Override
            public void success() {
                getModel(filePath);
            }

            @Override
            public void fail() {

            }
        });

    }

    /**
     * 查询视频文件
     *
     * @param filePath 指定查询的文件夹数组
     */
    public void getModel(String[] filePath) {
        for (int i = 0; i < filePath.length; i++) {
            File file = new File(filePath[i]);
            if (file != null && file.exists() && file.isDirectory()) {
                queryVideoFileFromDirectory(file);
            } else {
                Logger.e(TAG, "file not isExist");
                file.mkdirs();
            }
        }
        if (videoInfoArrayList != null && videoInfoArrayList.size() > 0) {
            Collections.sort(videoInfoArrayList);
            MyGridViewAdapter myGridViewAdpater = new MyGridViewAdapter(this, videoInfoArrayList);
            gv_video.setAdapter(myGridViewAdpater);
            gv_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    List<String> videoPathList = new ArrayList<String>();
                    if(videoInfoArrayList != null && videoInfoArrayList.size() > 0){
                        for (VideoInfo videoInfo : videoInfoArrayList) {
                            videoPathList.add(videoInfo.videoPath);
                        }
                    }
                    Intent i = new Intent(MainActivity.this, VideoPlayActivity.class);
                    i.putExtra("position", position);
                    i.putStringArrayListExtra("videoInfoList", (ArrayList<String>) videoPathList);
                    startActivity(i);

                    //隐式启动
//                    Uri uri = Uri.parse("file://xxx");
//                    intent.setDataAndType(uri,"video/*");
//                    Intent intent = new Intent("com.booyue.android.mediaplayer.video");
//                    intent.putStringArrayListExtra("videoInfoList", (ArrayList<String>) videoPathList);
//                    intent.putExtra("position", position);
//                    startActivity(intent);

//                    List<String> videoPathList = new ArrayList<String>();
//                    for (VideoInfo videoInfo : videoInfoArrayList) {
//                        videoPathList.add(videoInfo.videoPath);
//                    }
//                    Intent intent = new Intent();
//                    intent.setComponent(new ComponentName("com.tigercxvideo", "com.tigercxvideo.custom.VideoFragmentActivity"));
//                    intent.putExtra("player_type", 1);
//                    intent.putExtra("player_list", (Serializable) videoPathList);
//                    intent.putExtra("player_position", position);
//                    startActivity(intent);


                }
            });
            Logger.e(TAG, "setAdapter complete");
        } else {
            tips();
            Logger.e(TAG, "videoInfoArrayList == null || videoInfoArrayList.size() == 0");
        }
    }

    /**
     * 从指定的目录下查询视频文件
     *
     * @param file
     */
    private void queryVideoFileFromDirectory(File file) {
        if (file == null) return;
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {//有文件
            for (File f : files) {
                if (f == null) continue;
                if (f.isDirectory()) {
                    queryVideoFileFromDirectory(f);
                } else {
                    if (isVideoFileFormat(f)) {//符合视频文件格式
                        VideoInfo vi = new VideoInfo();
                        vi.videoName = f.getName();
                        vi.videoPath = f.getPath();
                        vi.imagePath = vi.videoPath.substring(0, vi.videoPath.lastIndexOf(".")) + ".png";
                        LoggerUtils.e(TAG + "---videoName : " + vi.videoName + "\n videoPath：" + vi.videoPath + "\n imagePath = " + vi.imagePath);
                        videoInfoArrayList.add(vi);
                    }


                }
            }
            LoggerUtils.e(TAG + "---videoInfoArrayList load data complete");
        } else {//没有文件
            tips();
            LoggerUtils.e(TAG + "---files == null || files.length == 0");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.img_back:
                finish();
        }
    }

    /**
     * 是否是视频文件格式
     *
     * @return true 是 false 不是
     */
    private boolean isVideoFileFormat(File f) {
        String[] formatArray = new String[]{"mp4", "mkv", "avi", "rm", "rmvb"};
        for (String s : formatArray) {
            if (f.getName().endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean isImageFileFormat(File f) {
        String[] formatArray = new String[]{"jpg", "jpeg", "png", "bmp"};
        for (String s : formatArray) {
            if (f.getName().endsWith(s)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 用户提示
     */
    private void tips(int text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    //用户提示
    private void tips() {
//        String text = filePath[0] + "或" + filePath[1] + getResources().getString(R.string.tips_empty_video);
//        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
