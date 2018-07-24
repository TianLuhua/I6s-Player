package com.booyue.karaoke.utils;

import android.content.Context;
import android.widget.ImageView;

import com.booyue.karaoke.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Created by Administrator on 2018/4/25.15:35
 */

public class ImageUtil {
    /**
     * 加载图片
     * @param context 上下文
     * @param path 图片路径
     * @param imageView 展示图片的view
     */
    public static void loadImage(Context context, String path, ImageView imageView){
        Glide.with(context).load(path).placeholder(R.drawable.icon_default).
                error(R.drawable.icon_default).diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
    }
}
