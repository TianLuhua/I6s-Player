package com.booyue.karaoke.PicturePlayer;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import com.booyue.karaoke.R;
import com.booyue.karaoke.base.BaseModel;
import com.booyue.karaoke.utils.ThreadPoolManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.callback.Callback;


/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class PicturePlayerModel implements BaseModel {

    private CallBack callback;
    private List<PhotoView> imageInfoList = new ArrayList<>();
    private List<String> imagePaths = new ArrayList<>();
    private int position;
    private Context mContext;


    public PicturePlayerModel(Context mContext, CallBack callBack) {
        this.callback = callBack;
        this.mContext = mContext;
    }

    public void getData(final Uri uri) {
        String path = uri.getPath();
        int startIndex = path.lastIndexOf("/");
        File rootFile = new File(path.substring(0, startIndex));
        String rootPath = rootFile.getPath();
        imageInfoList.clear();
        imagePaths.clear();
        for (String s : rootFile.list()) {
            String childPath = rootPath + "/" + s;
            imagePaths.add(childPath);
            //系统支持：jpg、png
            if (childPath.endsWith(".jpg") || childPath.endsWith(".png")) {
                View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_pictureactivity_viewpager, null);
                final PhotoView photoView = itemView.findViewById(R.id.item_image);
                Glide.with(mContext).load(childPath).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(0.1f).into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        photoView.setImageDrawable(resource.getCurrent());
                    }
                });
                photoView.setTag(childPath);
                imageInfoList.add(photoView);
            }
        }
        position = imagePaths.indexOf(path);
        if (callback == null)
            return;
        callback.setData(imageInfoList, position);
    }

    public int getCureentPosition() {
        return position;
    }

    @Override
    public void onDestroy() {
        if (imageInfoList != null)
            imageInfoList = null;
        if (callback != null)
            callback = null;
    }

    interface CallBack {
        void setData(List<PhotoView> imageInfoList, int position);
    }


}
