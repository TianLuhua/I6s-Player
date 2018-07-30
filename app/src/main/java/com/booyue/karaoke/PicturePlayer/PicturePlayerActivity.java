package com.booyue.karaoke.PicturePlayer;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.booyue.karaoke.R;
import com.booyue.karaoke.base.AbstractMVPActivity;
import com.booyue.karaoke.utils.LoggerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class PicturePlayerActivity extends AbstractMVPActivity<PicturePlayerView, PicturePlayerPersenter> implements PicturePlayerView, PicturePlayController.PicturePlayerUIListener {

    public final String TAG = PicturePlayerActivity.class.getSimpleName();

    private ImageView imageView;
    private PicturePlayController controller;

    private int position;
    private List<String> imageInfoList = new ArrayList<>();

    @Override
    protected int getContentViewID() {
        return R.layout.activity_picture;
    }

    @Override
    protected void initView() {
        imageView = findViewById(R.id.image);
        controller = new PicturePlayController(PicturePlayerActivity.this);
        controller.setPicturePlayerUIListener(this);
        controller.setAnchorView(imageView.getParent() instanceof View ? (View) imageView.getParent() : imageView);
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                controller.show();
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.show();
            }
        });

        getDataFromActivity();
        imageView.setImageBitmap(BitmapFactory.decodeFile(imageInfoList.get(position)));
    }


    @Override
    protected void onResume() {
        super.onResume();
        controller.show();
    }

    private void getDataFromActivity() {
        Uri uri = getIntent().getData();
        String path = uri.getPath();
        int startIndex = path.lastIndexOf("/");
        File rootFile = new File(path.substring(0, startIndex));
        String rootPath = rootFile.getPath();
        for (String s : rootFile.list()) {
            String childPath = rootPath + "/" + s;
            //系统支持：jpg、png
            if (childPath.endsWith(".jpg") || childPath.endsWith(".png")) {
                imageInfoList.add(childPath);
            }
        }
        position = imageInfoList.indexOf(path);
    }

    /**
     * 1.获取具体的bitmap
     * 2.更新界面UI
     */
    private void updateUI() {
        String videoPath = imageInfoList.get(position);
        int startIndex = videoPath.lastIndexOf("/");
        int endIndex = videoPath.lastIndexOf(".");
        String videoName = videoPath.substring(startIndex + 1, endIndex);
//        videoView.setName(videoName);
//        videoView.setVideoPath(videoPath);
//        videoView.requestFocus();
//        videoView.start();
        LoggerUtils.d(TAG + "视频路径：" + videoPath);
    }

    @Override
    protected PicturePlayerPersenter createPresenter() {
        return new PicturePlayerPersenter();
    }

    @Override
    public void onBack() {
        Log.e("PicturePlayerActivity", "onBack");
        this.finish();
    }

    @Override
    public void onPrev() {
        Log.e("PicturePlayerActivity", "onPrev");


    }

    @Override
    public void onNext() {
        Log.e("PicturePlayerActivity", "onNext");

    }

    @Override
    public void onRotate() {
        Log.e("PicturePlayerActivity", "onRotate");
    }

    @Override
    public void onPlay() {
        Log.e("PicturePlayerActivity", "onPlay");
    }
}
