package com.booyue.karaoke.PicturePlayer.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tianluhua on 2018\7\31 0031.
 */
public class PicturePlayAdapter extends PagerAdapter implements View.OnClickListener {

    private List<PhotoView> datas;

    public PicturePlayAdapter(List<PhotoView> datas) {
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }


    @Override
    //断是否由对象生成界面
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(datas.get(position));
        //每次滑动的时候把视图添加到viewpager
        PhotoView item = datas.get(position);
        item.setOnClickListener(this);
        return item;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub
        // 将当前位置的View移除
        container.removeView(datas.get(position));
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener == null)
            return;
        itemClickListener.onItemClick(v);
    }

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View v);
    }

}
