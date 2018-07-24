package com.booyue.karaoke.activity;

import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.booyue.karaoke.R;

public class KoraokeActivity extends BaseActivity implements View.OnClickListener {

    protected void initView() {
        super.initView();
        setContentView(R.layout.activity_karaoke);
        TextView tv_mtv = getViewById(R.id.tv_mtv);
        TextView tv_karaoke = getViewById(R.id.tv_karaoke);
        FrameLayout img_back = getViewById(R.id.img_back);
//        TextView tv_mtv = (TextView) findViewById(R.id.tv_mtv);
//        TextView tv_karaoke = (TextView) findViewById(R.id.tv_karaoke);
//        ImageButton img_back = (ImageButton) findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        tv_mtv.setOnClickListener(this);
        tv_karaoke.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Intent i = new Intent();
        i.setClass(this, MainActivity.class);
//        i.putExtra("isKaraoke", true);
        int id = v.getId();
        if (id == R.id.tv_mtv) {
            i.putExtra("isMtv", true);
        } else if (id == R.id.tv_karaoke) {
            i.putExtra("isMtv", false);
        }
        startActivity(i);
    }

}
