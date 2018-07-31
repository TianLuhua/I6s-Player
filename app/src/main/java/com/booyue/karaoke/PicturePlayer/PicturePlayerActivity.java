package com.booyue.karaoke.PicturePlayer;

import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.booyue.karaoke.PicturePlayer.adapter.PicturePlayAdapter;
import com.booyue.karaoke.R;
import com.booyue.karaoke.base.AbstractMVPActivity;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;


/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class PicturePlayerActivity extends AbstractMVPActivity<PicturePlayerView, PicturePlayerPersenter> implements PicturePlayerView, PicturePlayController.PicturePlayerUIListener, ViewPager.OnPageChangeListener {

    public final String TAG = PicturePlayerActivity.class.getSimpleName();

    private ViewPager viewPager;
    private PicturePlayController controller;

    private int position;
    private List<PhotoView> imageInfoList;
    private PhotoView currentPhotoView;

    private PicturePlayAdapter adapter;

    @Override
    protected int getContentViewID() {
        return R.layout.activity_picture;
    }

    @Override
    protected void initView() {
        viewPager = findViewById(R.id.viewpage);
        controller = new PicturePlayController(getApplicationContext());
        controller.setPicturePlayerUIListener(this);
        controller.setAnchorView(viewPager);
        viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                controller.show();
                viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        viewPager.addOnPageChangeListener(this);
        getPresenter().getData(getIntent().getData());

    }

    private void updateUI() {
        if (controller == null && imageInfoList == null && imageInfoList.size() > 0)
            return;
        controller.setCurrentFilePage(imageInfoList.size(), position);
        controller.setCurrentFileName((String) currentPhotoView.getTag());
    }


    @Override
    protected PicturePlayerPersenter createPresenter() {
        return new PicturePlayerPersenter(getApplicationContext());
    }

    @Override
    public void onBack() {
        Log.e("PicturePlayerActivity", "onBack");
        controller.hide();
        this.finish();
    }

    @Override
    public void onPrev() {
        int i = position--;
        this.position = i;
        if (i < 0)
            return;
        this.position = i;
        Log.e("PicturePlayerActivity", "onPrev---position:" + position);
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onNext() {
        Log.e("PicturePlayerActivity", "onNext");
        int i = position++;
        if (i > imageInfoList.size())
            return;
        this.position = i;
        Log.e("PicturePlayerActivity", "onNext---position:" + position);
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onRotate() {
        if (currentPhotoView == null) {
            Log.e("PicturePlayerActivity", "currentPhotoView == null");
            return;
        }
        currentPhotoView.setRotationBy(90);
        Log.e("PicturePlayerActivity", "onRotate");
    }

    @Override
    public void onPlay() {
        Log.e("PicturePlayerActivity", "onPlay");
        getPresenter().changPlayModle();
    }

    @Override
    public void setData(final List<PhotoView> imageInfoList, final int position) {
        this.imageInfoList = imageInfoList;
        this.position = position;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new PicturePlayAdapter(imageInfoList);
                adapter.setItemClickListener(new PicturePlayAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        controller.show();
                    }
                });
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(position, false);
                Log.e("PicturePlayerActivity", "setData--position:" + position);
            }
        });
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.e("PicturePlayerActivity", "onPageSelected---position：" + position);
        this.position = position + 1;
        this.currentPhotoView = imageInfoList.get(position);
        updateUI();
    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
