package com.booyue.karaoke.audioPaly.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.booyue.karaoke.R;
import com.booyue.karaoke.audioPaly.bean.AudioBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tianluhua on 2018\8\1 0001.
 */
public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioHolder> {

    private Context mContext;
    private List<AudioBean> audioBeans = new ArrayList<>();
    private int cureentPlayingItem=-1;


    public AudioListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setAudioBeans(List<AudioBean> audioBeans, int position) {
        this.audioBeans = audioBeans;
        this.notifyItemChanged(position);
    }

    @Override
    public AudioHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AudioHolder audioHolder = new AudioHolder(LayoutInflater.from(mContext).inflate(R.layout.item_audio_play_reclcer, null));
        return audioHolder;
    }

    @Override
    public void onBindViewHolder(AudioHolder holder, final int position) {
        final AudioBean audioBean = audioBeans.get(position);
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener == null)
                    return;
                onItemClickListener.onItemClick(audioBean, position);
            }
        });
        holder.icon.setVisibility(cureentPlayingItem==position ? View.VISIBLE : View.INVISIBLE);
        holder.name.setText(audioBean.getName());
    }

    @Override
    public int getItemCount() {
        return audioBeans == null ? 0 : audioBeans.size();
    }


    class AudioHolder extends RecyclerView.ViewHolder {

        private RelativeLayout item;
        private ImageView icon;
        private TextView name;

        public AudioHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item_content);
            icon = itemView.findViewById(R.id.item_icon);
            name = itemView.findViewById(R.id.item_name);
        }
    }


    public void setCureentPlayingItem(int position) {
        this.cureentPlayingItem=position;
        notifyDataSetChanged();
    }

    private OnItemClickListener onItemClickListener;

    /**
     * 设置item的点击监听器
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {

        void onItemClick(AudioBean audioBean, int position);

    }


}
