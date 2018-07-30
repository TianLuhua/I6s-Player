package com.booyue.karaoke.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Tianluhua on 2018/5/11.
 *
 * @param <V>
 * @param <P>
 */
public abstract class AbstractMVPFragment<V extends BaseView, P extends AbstractPresenter<V>>
        extends Fragment {

    protected P mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (mPresenter == null) {
            mPresenter = createPresenter();
        }
        if (mPresenter == null) {

            throw new NullPointerException("Presenter is null.....");
        }
        mPresenter.attachView((V) this);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }

    protected abstract P createPresenter();

    public P getPresenter() {
        return mPresenter;
    }

}
