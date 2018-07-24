package com.booyue.karaoke.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.booyue.karaoke.R;
import com.booyue.karaoke.bean.VideoInfo;
import com.booyue.karaoke.customview.RoundImageView;
import com.booyue.karaoke.utils.ImageUtil;
import com.booyue.karaoke.utils.VideoUtils;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2017/9/4.17:59
 */

public class MyGridViewAdapter extends MyBaseAdapter<VideoInfo> {
    private final VideoUtils videoUtils;

    public MyGridViewAdapter(Context context, List<VideoInfo> lists) {
        super(context, lists);
        videoUtils = new VideoUtils();
    }

    @Override
    public View getItemView(int position, View convertView, ViewGroup parent) {
        MyGridViewAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.video_item_view, null);
            holder = new MyGridViewAdapter.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final MyGridViewAdapter.ViewHolder vh = holder;
        VideoInfo curVideoInfo = ts.get(position);
        if (curVideoInfo == null) {
            return null;
        }
        final String path1 = curVideoInfo.videoPath;
        String videoName = curVideoInfo.videoName;
        holder.textView.setText(videoName.substring(0, videoName.lastIndexOf(".")));

        if (curVideoInfo.imagePath != null && new File(curVideoInfo.imagePath).exists()) {
            vh.imageView.setImageBitmap(BitmapFactory.decodeFile(curVideoInfo.imagePath));
            ImageUtil.loadImage(context, curVideoInfo.imagePath, vh.imageView);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    videoUtils.getImageFormVideo(new ReqeustBitmap() {
                        @Override
                        public void onBitmapCallback(String path, final Bitmap bitmap) {
                            if (path1.endsWith(path)) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        vh.imageView.setImageBitmap(bitmap);
                                    }
                                });
                            }
                        }
                    }, path1);
                }
            }).start();
        }
        return convertView;
    }

    class ViewHolder {
        RoundImageView imageView;
        TextView textView;

        public ViewHolder(View view) {
            this.imageView = (RoundImageView) view.findViewById(R.id.item_image);
            this.textView = (TextView) view.findViewById(R.id.item_name);
        }
    }
//
//    /**
//     * 图标点击监听
//     */
//    class ImageClickListener implements View.OnClickListener {
//        private VideoInfo curVideoInfo;
//        private int position;
//
//        public ImageClickListener(VideoInfo curVideoInfo,int position) {
//            this.curVideoInfo = curVideoInfo;
//            this.position = position;
//        }
//
//        @Override
//        public void onClick(View v) {
//            if (curVideoInfo != null) {
//
//                context.startActivity(i);
//            }
//        }
//    }

    public interface ReqeustBitmap {
        void onBitmapCallback(String filepath, Bitmap bitmap);
    }
}
