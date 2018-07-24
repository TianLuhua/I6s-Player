package com.booyue.karaoke.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.booyue.karaoke.R;

/**
 * 闪屏页
 * 展示公司品牌，宣传app的主要功能
 */
public class FlashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                enterActivity(KoraokeActivity.class, null);
            }
        }, 0);
    }

    /**
     * 进入下一个页面
     *
     * @param clazz  页面的类名
     * @param bundle 数据包
     */
    public void enterActivity(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        finish();
    }

}
