package wzk.com.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * @author WangZhengkui on 2015-12-15 18:46
 */
public class SlideView extends FrameLayout {
    public static final int SLIDE_STATUS_OFF = 0;
    public static final int SLIDE_STATUS_SCROLL = 1;
    public static final int SLIDE_STATUS_ON = 2;
    private int slideStatus = SLIDE_STATUS_OFF;
    Context mContext;
    int mDownX,mDownY;
    int mLastX,mLastY;
    int deltaX ,deltaY;
    FrameLayout contentFv;
    LinearLayout bottomLv;
    View[] views;
    int bottomWidth;
    public SlideView(Context context) {
        this(context, null);
    }

    public SlideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("NewApi")
    public SlideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

        View view  = inflate(mContext,R.layout.slide_view_merge,this);
        contentFv = (FrameLayout)view.findViewById(R.id.contentFv);
        bottomLv = (LinearLayout)view.findViewById(R.id.bottomLv);
        setClickable(true);
        post(new Runnable() {
            @Override
            public void run() {
                bottomWidth = bottomLv.getWidth();
                int height = contentFv.getMeasuredHeight();
                Log.i("SlideView","height = "+height+",measureHeight = "+contentFv.getMeasuredHeight());
                ViewGroup.LayoutParams layoutParams = bottomLv.getLayoutParams();
                layoutParams.height = height;
                bottomLv.setLayoutParams(layoutParams);
                if (bottomLv.getChildCount() > 0) {
                    for (int i = 0; i < bottomLv.getChildCount(); i++) {
                        measureChild(bottomLv.getChildAt(i),MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),MeasureSpec.makeMeasureSpec(height,MeasureSpec.AT_MOST));
                    }
                    bottomLv.requestLayout();
                }
            }
        });
    }

    public LinearLayout getBottomLv() {
        return bottomLv;
    }


    public void setBottomView(View... views) {
        if (views == null) {
            return;
        }
        this.views = views;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2,-1);
        for (int i = 0; i < views.length; i++) {
            bottomLv.addView(views[i],params);
        }
    }
    public void setContentView(View view) {
//        contentLv.addView(view,-1,-1);
        contentFv.addView(view, -1, -1);
    }

    public void handleMotionEvent(MotionEvent ev) {
        Log.i("SlideView", "action = " + ev.getAction() + "," + slideStatus);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();
                mLastX = (int) ev.getX();
                mLastY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                deltaX = (int) (mLastX - ev.getX());
                deltaY = (int) (mLastY - ev.getY());
                //设置该View此时的状态
                //记录x方向的差值
                final float xDiff = Math.abs(mDownX - ev.getX());
                final float yDiff = Math.abs(mDownY - ev.getY());
                if (xDiff > ViewConfiguration.get(mContext).getScaledTouchSlop() || yDiff > ViewConfiguration.get(mContext).getScaledTouchSlop()) {
                    Log.i("SlideView","move>slop");
                    //滑动范围控制
                    if ((contentFv.getScrollX() + deltaX) > bottomWidth) {
//                        scrollerToOpen(false);
                        contentFv.scrollTo(bottomWidth,0);
                    }else if ((contentFv.getScrollX() + deltaX) < 0) {
//                        scrollerToClose(false);
                        contentFv.scrollTo(0,0);
                    } else {
                        contentFv.scrollBy(deltaX, 0);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.i("SlideView", "deltaX="+deltaX+",deltaY="+deltaY);
                deltaX = (int) Math.abs(ev.getX() - mDownX);
                deltaY = (int) Math.abs(ev.getY() - mDownY);
                if (Math.abs(deltaX) <= ViewConfiguration.get(mContext).getScaledTouchSlop() && Math.abs(deltaY) <= ViewConfiguration.get(mContext).getScaledTouchSlop()) {
                    if (slideStatus == SLIDE_STATUS_OFF) {
                        //产生点击事件
                        performClick();
                    } else {
                        scrollerToClose(true);
                    }
                } else {
                    if (contentFv.getScrollX() > bottomWidth /2) {
                        scrollerToOpen(true);
                    } else {
                        scrollerToClose(true);
                    }
                }
                break;
        }
        mLastX = (int) ev.getX();
        mLastY = (int) ev.getY();
    }

    public void smoothTo(int destX,int destY) {
        // 缓慢滚动到指定位置
        int scrollX = contentFv.getScrollX();
        ValueAnimator animator = ValueAnimator.ofInt(scrollX,destX);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                contentFv.scrollTo((Integer) animation.getAnimatedValue(), 0);
                slideStatus = SLIDE_STATUS_SCROLL;
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (contentFv.getTag() != null) {
                    if ("open".equals(contentFv.getTag().toString())) {
                        slideStatus = SLIDE_STATUS_ON;
                    } else if ("close".equals(contentFv.getTag().toString())) {
                        slideStatus = SLIDE_STATUS_OFF;
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(100);
        animator.start();
//        postInvalidate();
    }
    public void scrollerToOpen(boolean smooth) {
        Log.i("SlideView", "scrollerToOpen");
        contentFv.setTag("open");
        if (smooth) {
            smoothTo(bottomWidth, 0);
        } else {
            contentFv.scrollTo(bottomWidth,0);
            slideStatus = SLIDE_STATUS_ON;
        }
    }
    public void scrollerToClose(boolean smooth) {
        contentFv.setTag("close");
        if (smooth) {
            smoothTo(0, 0);
        } else {
            contentFv.scrollTo(0, 0);
            slideStatus = SLIDE_STATUS_OFF;
        }
    }

    public int getSlideStatus() {
        return slideStatus;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();

    }

    /**
     * 在此方法中执行item删除之后，其他的item向上或者向下滚动的动画，并且将position回调到方法onDismiss()中
     */
    public void performDismiss(final int position, final OnDismissCallback callback) {
        final ViewGroup.LayoutParams lp = getLayoutParams();//获取item的布局参数
        final int originalHeight = getHeight();//item的高度

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0).setDuration(200);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scrollerToClose(false);
                if (callback != null) {
                    callback.onDismiss(position);
                }
                //这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
                //所以我们在动画执行完毕之后将item设置回来
                ViewHelper.setAlpha(SlideView.this, 1f);
                ViewHelper.setTranslationX(SlideView.this, 0);
                ViewGroup.LayoutParams lp = SlideView.this.getLayoutParams();
                lp.height = originalHeight;
                SlideView.this.setLayoutParams(lp);

            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                SlideView.this.setLayoutParams(lp);
            }
        });

    }

    /**
     * 删除的回调接口
     *
     * @author xiaanming
     *
     */
    public interface OnDismissCallback {
        public void onDismiss(int dismissPosition);
    }
}
