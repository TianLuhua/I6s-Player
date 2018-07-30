package com.booyue.karaoke.PicturePlayer;

import android.graphics.Bitmap;

import com.booyue.karaoke.base.BaseView;

import java.util.List;

/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public interface PicturePlayerView extends BaseView {

    void setData(List<String> imageInfoList,int position);

    void setData(Bitmap bitmap, String name, int total, int position);
}
