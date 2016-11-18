package com.crystal.simpleslidinguppanel.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.crystal.simpleslidinguppanel.R;

/**
 * 实现向上拖动额外添加面板panel，可覆盖上一层
 * Created by crystalchi on 2016/11/17 0017.
 */

public class SimpleSlidingUpPanel extends ViewGroup{

    private static final String TAG = SimpleSlidingUpPanel.class.getSimpleName();
    private static final float SENSITIVITY = 1.0f;
    private ViewDragHelper mViewDragHelper; //手势拖动帮助类
    private int mDragHeadPanelHeight; //拖动的头部部分的view高度

    private View mMainView;
    private View mDragView;
    private View mDragHeadView;
    private ListView mScrollableView;
    private int mTop; //拖动panel时的top值(距离屏幕顶部)
    private int mDragRange; //panel能拖动的最大范围值
    private float mDragOffset; //panel拖动的偏移比率

    private static final float TAG_VALUE = 1.0f;

    public SimpleSlidingUpPanel(Context context) {
        this(context, null);
    }

    public SimpleSlidingUpPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleSlidingUpPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Get the width and height of the current ViewGroup.
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Get two childview from the current ViewGroup.
        mMainView = getChildAt(0);
        mDragView = getChildAt(1);

        //<1>. Measure the mainview.
        //Get the paddingLeft and paddingRight
        //and paddingTop and paddingBottom of the current ViewGroup.
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        //Get the layoutParams of the mainview.
        MarginLayoutParams lp = (MarginLayoutParams) mMainView.getLayoutParams();
        //Get the actual width of the mainview.
        int mainviewWidth = widthSize - lp.leftMargin - lp.rightMargin - paddingLeft - paddingRight;
        //Get the actual height of the mainview.
        int mainviewHeight = heightSize - lp.topMargin - lp.bottomMargin - paddingTop - paddingBottom;

        int mainviewWidthSpec = MeasureSpec.makeMeasureSpec(mainviewWidth, MeasureSpec.EXACTLY);
        int mainviewHeightSpec = MeasureSpec.makeMeasureSpec(mainviewHeight, MeasureSpec.EXACTLY);

        //Measure the child view of the current ViewGroup.
        //Measure the mainview.
        mMainView.measure(mainviewWidthSpec, mainviewHeightSpec);

        //<2>. Measure the dragview.
        lp = (MarginLayoutParams) mDragView.getLayoutParams();
        //Get the actual width of the dragview.
        int dragviewWidth = widthSize - lp.leftMargin - lp.rightMargin - paddingLeft - paddingRight;
        //Get the actual height of the dragview.
        int dragviewHeight = heightSize - lp.topMargin - lp.bottomMargin;

        int dragviewWidthSpec = MeasureSpec.makeMeasureSpec(dragviewWidth, MeasureSpec.EXACTLY);
        int dragviewHeightSpec = MeasureSpec.makeMeasureSpec(dragviewHeight, MeasureSpec.EXACTLY);
        //Measure the dragview.
        mDragView.measure(dragviewWidthSpec, dragviewHeightSpec);

        //Set the size of the current ViewGroup.
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        //Get the paddingLeft and paddingTop of the current ViewGroup.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        //Set the layout location of the mainview.
        MarginLayoutParams  lp = (MarginLayoutParams) mMainView.getLayoutParams();
        mMainView.layout(
                lp.leftMargin + paddingLeft,
                lp.topMargin + paddingTop,
                lp.leftMargin + paddingLeft + mMainView.getMeasuredWidth(),
                lp.topMargin + paddingTop + mMainView.getMeasuredHeight()
        );

        //Set the layout location of the dragview.
        lp = (MarginLayoutParams) mDragView.getLayoutParams();
        //Get the top of the dragview.
        int dragViewTop = getMeasuredHeight() - mDragHeadPanelHeight - lp.topMargin - paddingTop;
        mDragView.layout(
                lp.leftMargin + paddingLeft,
                dragViewTop,
                lp.leftMargin + paddingLeft + mDragView.getMeasuredWidth(),
                dragViewTop + mDragView.getMeasuredHeight()
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDragRange = getHeight() - mDragHeadPanelHeight;
    }

    /**
     * 拦截事件交给ViewDragHelper
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        final float x = ev.getX();
        final float y = ev.getY();
        boolean interceptTap = false;
        switch (action){
            case MotionEvent.ACTION_DOWN:
                //判断触摸的是DragHeadView
                if(isViewUnder(mDragHeadView, (int) x, (int) y)){
                    interceptTap = true;
                }
                break;
        }
        if(mTop == 0 && mScrollableView.getFirstVisiblePosition() != 0){
            mScrollableView.requestDisallowInterceptTouchEvent(true);
        }
        //拦截事件并将事件交给ViewDragHelper
        boolean interceptResult = mViewDragHelper.shouldInterceptTouchEvent(ev);
        Log.d(TAG, "interceptResult is " + interceptResult);
        return interceptResult || interceptTap;
    }

    /**
     * 在此事件中传递事件给ViewDragHelper
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 加载完毕，在这个方法中可以获取布局中的view
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragHeadView = findViewById(R.id.drag_head_view);
        mScrollableView = (ListView) findViewById(R.id.lv_dragview);
    }

    /**
     * 为添加到当前ViewGroup的子View添加与当前ViewGroup匹配的LayoutParams
     * @param p
     * @return
     */
    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    /**
     * 为添加到当前ViewGroup的子View添加与当前ViewGroup匹配的LayoutParams
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 为添加到当前ViewGroup的子View添加与当前ViewGroup匹配的LayoutParams
     * @return
     */
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public class ViewDragHelperCallback extends ViewDragHelper.Callback{

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            mTop = top;
            //实时计算panel偏移比率
            mDragOffset = calDragPanelOffset(top);
            Log.d(TAG, "mTop is " + mTop);
        }

        /**
         * 控制垂直方向的界限
         * @param child
         * @param top
         * @param dy
         * @return
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int topBound = getPaddingTop();
            int bottomBound = getHeight() - mDragHeadPanelHeight;

            if(top < topBound)
                top = topBound;

            if(top > bottomBound)
                top = bottomBound;

            return top;
        }

        //加上这一句，shouldInterceptTouchEvent才会返回true。看源码就知道问题所在。
        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragRange;
        }

        /**
         *
         * @param releasedChild
         * @param xvel
         * @param yvel //y轴方向上的速率
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            int top = 0;
            float direction = yvel; //direction > 0 代表向下拖动，direction < 0 代表向上拖动。
            if(direction < 0 && mDragOffset <= TAG_VALUE){
                top = calPanelTop(TAG_VALUE);
            }else if(mDragOffset >= TAG_VALUE / 2 && mDragOffset <= TAG_VALUE / 10 * 8){
                top = calPanelTop(TAG_VALUE / 2);
            }else if(direction > 0 && mDragOffset <= TAG_VALUE){
                top = calPanelTop(0.0f);
            }else if(mDragOffset > TAG_VALUE / 2){
                top = calPanelTop(TAG_VALUE);
            }else if(mDragOffset < TAG_VALUE / 2){
                top = calPanelTop(0.0f);
            }
            mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
            invalidate();
        }
    }


    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 初始化参数
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs){
        mViewDragHelper = ViewDragHelper.create(this, SENSITIVITY, new ViewDragHelperCallback());
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleSlidingUpPanel);
        if(null != ta){
            mDragHeadPanelHeight = ta.getDimensionPixelSize(R.styleable.SimpleSlidingUpPanel_dragHeadPanelHeight, -1);
        }
        ta.recycle();
    }

    /**
     * 计算Panel拖动的偏移比率
     * 以向上拖动为标准，偏移比率从0.0 — 1.0递增；
     * 那么向下拖动，偏移比率就应该从1.0 — 0.0递减。
     * 所以向上方向拖动，偏移比率计算公式应该为：
     * (float)(mDragRange - top) / (float) mDragRange;
     * 那向下偏移是不是就应该为：(float)(top) / (float) mDragRange;
     * 答案是否定的，你可以输入数据计算，向下按照此公式计算，偏移比率是从0.0 ― 1.0。
     * 结果是递增！这是不符合我们偏移所要实现的效果的。
     * 所以向下偏移比率公式就该为：(float)(mDragRange - top) / (float) mDragRange;
     * 偏移比率结果是递减，符合我们要实现的效果。
     * 所以公式统一为：(float)(mDragRange - top) / (float) mDragRange;
     * @param top
     */
    private float calDragPanelOffset(int top){
        return (float)(mDragRange - top) / (float) mDragRange;
    }

    /**
     * 根据panel的拖动的偏移比率计算拖动Panel的当前Top值
     * @param dragOffset
     * @return
     */
    private int calPanelTop(float dragOffset){
       return  mDragRange - (int)(dragOffset * mDragRange);
    }

    /**
     * 判断触摸的是DragHeadView
     * @param view
     * @param x
     * @param y
     * @return
     */
    private boolean isViewUnder(View view, int x, int y) {
        if (view == null) return false;
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        //判断触摸点是在view范围内，其实就是触摸就是DragHeadView.
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
    }

}
