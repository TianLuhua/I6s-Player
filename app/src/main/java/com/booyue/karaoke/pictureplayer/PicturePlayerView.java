package com.booyue.karaoke.pictureplayer;

import com.booyue.karaoke.base.BaseView;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public interface PicturePlayerView extends BaseView {

    void setData(List<PhotoView> imageInfoList, int position);

    void setCureentPage(int page, int palyModle);
}
