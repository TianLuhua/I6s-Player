package com.booyue.karaoke.audioPaly;

import com.booyue.karaoke.audioPaly.bean.AudioBean;
import com.booyue.karaoke.base.BaseView;

import java.util.List;

/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public interface AudioPlayerView extends BaseView {

    void setData(List<AudioBean> audioInfoList, int position);

    void startPlay(String uri, int position);
}
