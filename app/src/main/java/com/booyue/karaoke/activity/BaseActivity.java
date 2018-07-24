package com.booyue.karaoke.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.booyue.karaoke.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/4.17:54
 */

public class BaseActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 100;
    public String TAG = this.getClass().getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.e(TAG, "----onCreate----");
        fullScreen();
        initView();
        initData();
    }

    public <T> T getViewById(int resId) {
        return (T) findViewById(resId);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.e(TAG, "----onRestart----");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.e(TAG, "----onStart----");
    }

    protected void onResume() {
        super.onResume();
        Logger.e(TAG, "----onResume----");
    }

    protected void onPause() {
        super.onPause();
        Logger.e(TAG, "----onPause----");
    }

    protected void onStop() {
        super.onStop();
        Logger.e(TAG, "----onStop----");
    }

    protected void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "----onDestroy----");
    }

    protected void initView() {
    }

    protected void initData() {
    }


    private void fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private List<String> needPerssions = new ArrayList<>();
    private PermissionListener listener;

    public void checkPermission(String[] permissions, PermissionListener listener) {
        needPerssions.clear();
        this.listener = listener;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                needPerssions.add(permission);
            }
        }
        if (needPerssions.size() != 0) {
            ActivityCompat.requestPermissions(this, needPerssions.toArray(new String[needPerssions.size()]), REQUEST_PERMISSION_CODE);
        } else {
            listener.success();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int permission : grantResults) {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    if (listener != null) {
                        listener.fail();
                        return;
                    }
                }
            }
            if (listener != null) {
                listener.success();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    interface PermissionListener {
        void success();

        void fail();
    }

    /**
     * 进入下一个activity页面
     *
     * @param clazz  进入页面的类名
     * @param bundle 需要携带的数据包
     */
    public void enterActivity(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }
}
