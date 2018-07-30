package com.booyue.karaoke.PicturePlayer;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.booyue.karaoke.R;

import java.lang.reflect.Method;

import io.vov.vitamio.utils.Log;

/**
 * Created by Tianluhua on 2018\7\30 0030.
 */
public class PicturePlayController extends FrameLayout implements View.OnClickListener {

    private static final int FADE_OUT = 0x0001;
    private static final int DEFAULTTIMEOUT = 3000;

    private View mAnchor;
    private View mRoot;

    private Context mContext;
    private boolean mFromXml = false;
    private boolean mShowing;
    private PopupWindow mWindow;
    private int mAnimStyle;


    private FrameLayout flPictrue;
    private ImageView back;
    private ImageView play;
    private ImageView rotate;
    private ImageView pre;
    private ImageView next;
    private TextView name;
    private TextView page;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
            }
        }
    };

    /**
     * 从xml布局中调用
     *
     * @param context
     * @param attrs
     */
    public PicturePlayController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mFromXml = true;
        initController(context);
    }

    /**
     * 用户手动实例化
     *
     * @param context
     */
    public PicturePlayController(Context context) {
        super(context);
        Log.e("PicturePlayController---PicturePlayController");
        if (!mFromXml && initController(context))
            initFloatingWindow();

    }

    private boolean initController(Context context) {
        Log.e("PicturePlayController---initController");
        mContext = context.getApplicationContext();
        return true;
    }

    private void initFloatingWindow() {
        Log.e("PicturePlayController---initFloatingWindow");
        mWindow = new PopupWindow(mContext);
        mWindow.setFocusable(false);
        mWindow.setBackgroundDrawable(null);
        mWindow.setOutsideTouchable(true);
        mAnimStyle = android.R.style.Animation;
    }


    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        Log.e("PicturePlayController---onFinishInflate");
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public void setAnchorView(View view) {
        Log.e("PicturePlayController---setAnchorView");
        mAnchor = view;
        if (!mFromXml) {
            removeAllViews();
            mRoot = makeControllerView();
            mWindow.setContentView(mRoot);
            mWindow.setWidth(LayoutParams.MATCH_PARENT);
            mWindow.setHeight(LayoutParams.MATCH_PARENT);
        }
        initControllerView(mRoot);
    }

    protected View makeControllerView() {
        Log.e("PicturePlayController---makeControllerView");
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(getResources().getIdentifier("picturecontroller_karaoke", "layout", mContext.getPackageName()), this);
    }

    private void initControllerView(View v) {
        flPictrue = v.findViewById(R.id.fl_picture);
        back = v.findViewById(R.id.back);
        play = v.findViewById(R.id.play);
        rotate = v.findViewById(R.id.rotate);
        pre = v.findViewById(R.id.pre);
        next = v.findViewById(R.id.next);
        name = v.findViewById(R.id.name);
        page = v.findViewById(R.id.page);
        initListener();
    }

    private void initListener() {
        flPictrue.setOnClickListener(this);
        back.setOnClickListener(this);
        play.setOnClickListener(this);
        rotate.setOnClickListener(this);
        pre.setOnClickListener(this);
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                if (mPicturePlayerUIListener == null)
                    return;
                mPicturePlayerUIListener.onBack();
                break;
            case R.id.play:
                if (mPicturePlayerUIListener == null)
                    return;
                mPicturePlayerUIListener.onPlay();
                show();
                break;
            case R.id.rotate:
                if (mPicturePlayerUIListener == null)
                    return;
                mPicturePlayerUIListener.onRotate();
                show();
                break;
            case R.id.pre:
                if (mPicturePlayerUIListener == null)
                    return;
                mPicturePlayerUIListener.onPrev();
                show();
                break;
            case R.id.next:
                if (mPicturePlayerUIListener == null)
                    return;
                mPicturePlayerUIListener.onNext();
                show();
                break;
            case R.id.fl_picture:
                Log.e("PicturePlayController---flPictrue:" + mShowing);
                if (isShowing()) {
                    hide();
                } else {
                    show(DEFAULTTIMEOUT);
                }
                break;
        }
    }


    public void hide() {
        if (mAnchor == null)
            return;
        if (mShowing) {
            try {
                if (mFromXml)
                    setVisibility(View.GONE);
                else
                    mWindow.dismiss();
            } catch (IllegalArgumentException ex) {
                Log.d("PictureController already removed");
            }
            mShowing = false;
            if (mHiddenListener != null)
                mHiddenListener.onHidden();
        }
    }

    public void show(int timeout) {
        Log.e("PicturePlayController---1111----show:" + timeout);
        if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {
            if (play != null)
                play.requestFocus();
            if (mFromXml) {
                Log.e("PicturePlayController---show-------FromXml-----");
                setVisibility(View.VISIBLE);
            } else {
                int[] location = new int[2];
                mAnchor.getLocationOnScreen(location);
                Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1] + mAnchor.getHeight());
                mWindow.setAnimationStyle(mAnimStyle);
                setWindowLayoutType();
                mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, anchorRect.left, anchorRect.bottom);
                Log.e("PicturePlayController---show------------");
            }
            mShowing = true;
            if (mShownListener != null)
                mShownListener.onShown();
        } else {
            Log.e("PicturePlayController----------222222222---------show");
        }
        updatePausePlay();
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
        }
    }

    public void show() {
        Log.e("PicturePlayController-------show:" + mShowing);
        show(DEFAULTTIMEOUT);
    }


    public void setWindowLayoutType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                Method setWindowLayoutType = PopupWindow.class.getMethod("setWindowLayoutType", new Class[]{int.class});
                setWindowLayoutType.invoke(mWindow, WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
            } catch (Exception e) {
                Log.e("setWindowLayoutType", e);
            }
        }
    }


    private void updatePausePlay() {
        if (mRoot == null || play == null)
            return;
//        if (play.isPlaying())
//            ibPlay.setImageResource(R.mipmap.ic_puse);
//        else
//            ibPlay.setImageResource(R.mipmap.ic_play);
//        initPlayModeIcon(false);
    }

    public boolean isShowing() {
        return mShowing;
    }

    private OnShownListener mShownListener;

    public void setOnShownListener(OnShownListener l) {
        mShownListener = l;
    }

    private OnHiddenListener mHiddenListener;

    public void setOnHiddenListener(OnHiddenListener l) {
        mHiddenListener = l;
    }

    public interface OnShownListener {
        void onShown();
    }

    public interface OnHiddenListener {
        void onHidden();
    }

    /**
     * 定义activity需要监听ui的接口
     */
    public interface PicturePlayerUIListener {
        //退出
        void onBack();

        //上一页
        void onPrev();

        //下一页
        void onNext();

        //旋转
        void onRotate();

        //循环播放
        void onPlay();
    }

    private PicturePlayerUIListener mPicturePlayerUIListener;

    public void setPicturePlayerUIListener(PicturePlayerUIListener listener) {

        mPicturePlayerUIListener = listener;
    }


    /**
     * 资源释放
     */
    public void release() {
        if (isShowing()) {
            hide();
        }
        if (mPicturePlayerUIListener != null) {
            mPicturePlayerUIListener = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mAnchor != null) {
            mAnchor = null;
        }
    }
}
