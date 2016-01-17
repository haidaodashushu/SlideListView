package wzk.com.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

/**
 * @author WangZhengkui on 2015-12-15 18:47
 */
public class SlideListView extends ListView {
    public static boolean isSlide;
    public SlideView slideView;
    Context mContext;
    SlideView focusView;
    int mDownX, mDownY;
    int mLastX, mLastY;
    int deltaX, deltaY;
    boolean isDecide;
    /**
     * 是否向子View传递MotionEvent,true则传递给子View，false则自己处理MotionEvent
     */
    private boolean dispatch;
    private int mTouchSlopX;
    private int mTouchSlopY;

    public SlideListView(Context context) {
        this(context, null);
    }

    public SlideListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mTouchSlopX = 50;
//        mTouchSlopX = ViewConfiguration.get(context).getScaledTouchSlop();
        mTouchSlopY = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public SlideView getSlideView() {
        return slideView;
    }

    public void setSlideView(SlideView slideView) {
        this.slideView = slideView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int index = pointToPosition((int) ev.getX(), (int) ev.getY());
        if (index == INVALID_POSITION) {
            return super.onInterceptTouchEvent(ev);
        }
        focusView = (SlideView) getChildAt(index - getFirstVisiblePosition());
        focusView.requestFocus();
        if (slideView == null) {
            return true;
        }

        if (slideView.getSlideStatus() == SlideView.SLIDE_STATUS_ON) {
            if (focusView == slideView && inRangeOfView(slideView.getBottomLv(), ev)) {
                return super.onInterceptTouchEvent(ev);
            } else {
                return true;
            }
        } else {
            return true;
        }

    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getRawX() > x && ev.getRawX() < (x + view.getWidth()) && ev.getRawY() > y && ev.getRawY() < (y + view.getHeight())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int actionMasked = ev.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();
                mLastX = mDownX;
                mLastY = mDownY;
                dispatch = false;
                isDecide = false;
                int index = pointToPosition((int) ev.getX(), (int) ev.getY());
                if (index == INVALID_POSITION) {
                    focusView = null;
                    if (isSlideViewOn(slideView)) {
                        slideView.scrollerToClose(true);
                        isSlide = false;
                    }
                    break;
                }
                Log.i("SlideListView", "INVALID");
                focusView = (SlideView) getChildAt(index - getFirstVisiblePosition());
                //如果slideView是打开的,并且再次点击的view跟slideView相等，则继续传递事件，否则将slideView关闭并禁止listView滑动
                if (isSlideViewOn(slideView) && slideView != focusView) {
                    slideView.scrollerToClose(true);
                    isSlide = false;
                } else {
                    focusView.handleMotionEvent(ev);
                    isSlide = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (focusView == null) {
                    break;
                }
                deltaX = (int) (mLastX - ev.getX());
                deltaY = (int) (mLastY - ev.getY());
                //如果isSlide为false，则意味着已经有子View是打开的状态，那么这次MotionEvent默认处理者是子View,该listView不滑动
                //如果isSlide为true,则有两种可能：1、子View都是关闭的状态。2、这次点击的view是处于打开状态的子View(即slideView == focusView)
                if (!isSlide) {
                    return true;
                }

                Log.i("SlideListView","focusVIew.statu = "+focusView.getSlideStatus());
                //如果点击的view是处于打开状态的子View
                if (slideView == focusView&&isSlideViewOn(slideView)) {
                    focusView.handleMotionEvent(ev);
                    return false;
                } else {
                    //子View都是关闭状态
                    //在mTouchSlop的距离内判断是否谁来处理MotionEvent,如果超出了距离，则不在进行判断（isDecide == true）
                    //记录x方向的差值
                    final float xDiff = Math.abs(mDownX - ev.getX());
                    final float yDiff = Math.abs(mDownY - ev.getY());
                    if (xDiff < mTouchSlopX && yDiff < mTouchSlopY && !isDecide) {
                        //如果超出了mTouchSlop，则停止xDiff计算，保持MotionEvent的处理者能一直处理
                        mLastX = (int) ev.getX();
                        //dispatch==true，则为左右滑动，将事件向后传递，listView不滑动
                        dispatch = xDiff > yDiff * 2;
                    } else {
                        isDecide = true;
                        if (dispatch) {
                            focusView.handleMotionEvent(ev);
                            slideView = focusView;
                            return true;
                        }
                    }
                }

//                Log.i("SlideListView", "x=" + xDiff + ",y=" + yDiff+",dispatch="+ dispatch);
                break;
            case MotionEvent.ACTION_UP:
                if (focusView != null) {
                    deltaX = (int) Math.abs(ev.getX() - mDownX);
                    deltaY = (int) Math.abs(ev.getY() - mDownY);
                    if (isSlide) {
                        if (slideView == focusView) {
                            focusView.handleMotionEvent(ev);
                            return true;
                        } else if (deltaX <= ViewConfiguration.get(mContext).getScaledTouchSlop() && deltaY <= ViewConfiguration.get(mContext).getScaledTouchSlop()) {
                            focusView.handleMotionEvent(ev);
                            return true;
                        }
                    }

                }
                break;
        }
        return super.onTouchEvent(ev);
//        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 判断slideView是否是打开状态，true表示为ON或者SCROLLER false表示OFF
     *
     * @param slideView
     * @return
     */
    public boolean isSlideViewOn(SlideView slideView) {
        return slideView != null && slideView.getSlideStatus() != SlideView.SLIDE_STATUS_OFF;
    }
}
