package com.booyue.karaoke.pictureplayer;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver;

import com.booyue.karaoke.pictureplayer.adapter.PicturePlayAdapter;
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
        if (controller == null && imageInfoList == null && imageInfoList.size() > 0) {
            return;
        }
        //显示的时候需要+1
        controller.setCurrentFilePage(imageInfoList.size(), viewPager.getCurrentItem() + 1);
        controller.setCurrentFileName(getPresenter().getImagePaths(viewPager.getCurrentItem()));
    }


    @Override
    protected PicturePlayerPersenter createPresenter() {
        return new PicturePlayerPersenter(getApplicationContext());
    }

    @Override
    public void onBack() {
        if (controller.isShowing())
            controller.hide();
        this.finish();
    }

    @Override
    public void onPrev() {
        if (position < 0) {
            return;
        }
        position--;
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onNext() {
        if (position > imageInfoList.size() - 1) {
            return;
        }
        position++;
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onRotate() {
        if (currentPhotoView == null) {
            return;
        }
        currentPhotoView.setRotationBy(90);
    }

    @Override
    public void onPlay() {
        getPresenter().changPlayModle();
    }

    @Override
    public void setData(final List<PhotoView> imageInfoList, final int position) {
        this.imageInfoList = imageInfoList;
        this.position = position + 1;
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
            }
        });
        updateUI();
    }

    @Override
    public void setCureentPage(int page, int palyModle) {
        viewPager.setCurrentItem(page, false);
        controller.updatePausePlay(palyModle == PicturePlayController.PLAYMODLE_LOOP);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
        this.currentPhotoView = imageInfoList.get(position);
        getPresenter().setCureentPage(position);
        updateUI();
    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null)
            controller.release();
    }
}
