package com.booyue.karaoke.base;

/**
 * Created by Tianluhua on 2018/5/11.
 *
 * @param <V>
 */
public abstract class AbstractPresenter<V extends BaseView> {

    private V mView;

    /**
     * Bind View
     *
     * @param view
     */
    public void attachView(V view) {
        this.mView = view;
    }

    /**
     * Unbind View
     */
    public void detachView() {
        this.mView = null;
    }

    /**
     * Get View
     *
     * @return
     */
    public V getView() {
        return mView;
    }

}
