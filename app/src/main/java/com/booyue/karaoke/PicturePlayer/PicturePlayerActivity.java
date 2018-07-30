package com.booyue.karaoke.PicturePlayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.booyue.karaoke.R;
import com.booyue.karaoke.base.AbstractMVPActivity;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;


/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class PicturePlayerActivity extends AbstractMVPActivity<PicturePlayerView, PicturePlayerPersenter> implements PicturePlayerView, PicturePlayController.PicturePlayerUIListener {

    public final String TAG = PicturePlayerActivity.class.getSimpleName();

    private PhotoView imageView;
    private PicturePlayController controller;

    private int position;
    private List<String> imageInfoList;

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

        getPresenter().getData(getIntent().getData());

    }


    /**
     * 1.获取具体的bitmap
     * 2.更新界面UI
     */
    private void updateUI() {
        String videoPath = imageInfoList.get(position);
        int startIndex = videoPath.lastIndexOf("/");
        int endIndex = videoPath.lastIndexOf(".");
        String imageName = videoPath.substring(startIndex + 1, endIndex);
        if (controller == null)
            return;
        controller.setCurrentFileName(imageName);
        controller.setCurrentFilePage(imageInfoList.size(), position);
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
        getPresenter().getData(position--);
    }

    @Override
    public void onNext() {
        Log.e("PicturePlayerActivity", "onNext");
        getPresenter().getData(position++);
    }

    @Override
    public void onRotate() {
        imageView.setRotationBy(90);
        Log.e("PicturePlayerActivity", "onRotate");
    }

    @Override
    public void onPlay() {
        Log.e("PicturePlayerActivity", "onPlay");
        getPresenter().changPlayModle();
    }

    @Override
    public void setData(final List<String> imageInfoList, final int position) {
        this.imageInfoList = imageInfoList;
        this.position = position;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(BitmapFactory.decodeFile(imageInfoList.get(position)));
                updateUI();
            }
        });
    }

    @Override
    public void setData(final Bitmap bitmap, final String name, final int total, final int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("PicturePlayerActivity", "position:"+position);
                imageView.setImageBitmap(bitmap);
                controller.setCurrentFileName(name);
                controller.setCurrentFilePage(total, position);
            }
        });

    }
}
