package com.booyue.karaoke.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.booyue.karaoke.utils.HideSystemUIUtils;

/**
 * Created by Tianluhua on 2018/5/11.
 *
 * @param <V>
 * @param <P>
 */
public abstract class AbstractMVPActivity<V extends BaseView, P extends AbstractPresenter<V>>
        extends AppCompatActivity {

    private P mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        HideSystemUIUtils.hideSystemUI(this);
        //在清单配置文件强制activity横屏导致启动应用慢的问题
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //保持屏幕常亮状态，系统屏幕休眠也会失效
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mPresenter == null) {
            mPresenter = createPresenter();
        }
        if (mPresenter == null) {

            throw new NullPointerException("Presenter is null.....");
        }
        mPresenter.attachView((V) this);
        setContentView(getContentViewID());
        initView();
    }

    protected abstract int getContentViewID();

    protected abstract void initView();

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mPresenter != null) {

            mPresenter.detachView();
        }
    }

    protected abstract P createPresenter();

    /**
     * Get Presenter
     *
     * @return
     */
    public P getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
