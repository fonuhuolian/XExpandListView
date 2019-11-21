package org.fonuhuolian.xexpandlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

public class XExpandListView extends ExpandableListView implements AbsListView.OnScrollListener {

    public XExpandListView(Context context) {
        super(context);
        iniView();
    }

    public XExpandListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        iniView();
    }

    private void iniView() {
        registerListener();
    }


    private static final int MAX_ALPHA = 255;

    private HoverAdapter mAdapter;

    /**
     * 用于在列表头显示的 View,mHeaderViewVisible 为 true 才可见
     */
    private View mHeaderView;

    /**
     * 列表头是否可见
     */
    private boolean mHeaderViewVisible;

    private int mHeaderViewWidth;

    private int mHeaderViewHeight;

    private List<View> headerClickViews = new ArrayList<>();
    // 当前头布局的坐标
    private int mGroupPos;

    private OnHoverClickListener listener;

    public void setHeaderView(View view) {
        setHeaderView(view, null);
    }

    public void setHeaderView(View view, OnHoverClickListener listener) {

        mHeaderView = view;

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }

        requestLayout();


        headerClickViews.clear();

        int[] idsArr = listener.getIdsArr();

        if (idsArr != null && idsArr.length > 0) {

            for (int i = 0; i < idsArr.length; i++) {
                View clickView = mHeaderView.findViewById(idsArr[i]);
                boolean b = !headerClickViews.contains(clickView);
                if (clickView != null && b) {
                    headerClickViews.add(clickView);
                }
            }

        }

        boolean isHasHeader = false;
        for (int i = 0; i < headerClickViews.size(); i++) {
            if (mHeaderView == headerClickViews.get(i)) {
                isHasHeader = true;
                break;
            }
        }

        if (!isHasHeader)
            headerClickViews.add(mHeaderView);

        this.listener = listener;
    }

    private void registerListener() {
        setOnScrollListener(this);
    }

    /**
     * 重写onMeasure计算悬浮条的大小
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mHeaderView != null)
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
    }

    /**
     * 列表界面更新时调用该方法(如滚动时)
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            //分组栏是直接绘制到界面中，而不是加入到ViewGroup中
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    public void configureHeaderView(int groupPosition, int childPosition) {
        if (mHeaderView == null || mAdapter == null
                || ((ExpandableListAdapter) mAdapter).getGroupCount() == 0) {
            return;
        }

        int state = mAdapter.getHeaderState(groupPosition, childPosition);

        switch (state) {
            case HoverAdapter.PINNED_HEADER_GONE: {
                mHeaderViewVisible = false;
                mGroupPos = -1;
                break;
            }

            case HoverAdapter.PINNED_HEADER_VISIBLE: {
                mAdapter.updateHeader(mHeaderView, groupPosition, childPosition, MAX_ALPHA);

                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }

                mHeaderViewVisible = true;
                mGroupPos = groupPosition;
                break;
            }

            case HoverAdapter.PINNED_HEADER_PUSHED_UP: {
                View firstView = getChildAt(0);
                int bottom = firstView.getBottom();

                // intitemHeight = firstView.getHeight();
                int headerHeight = mHeaderView.getHeight();

                int yOffset;

                int alpha;

                if (bottom < headerHeight) {
                    yOffset = (bottom - headerHeight);
                    alpha = MAX_ALPHA * (headerHeight + yOffset) / headerHeight;
                } else {
                    yOffset = 0;
                    alpha = MAX_ALPHA;
                }

                mAdapter.updateHeader(mHeaderView, groupPosition, childPosition, alpha);

                if (mHeaderView.getTop() != yOffset) {
                    mHeaderView.layout(0, yOffset, mHeaderViewWidth, mHeaderViewHeight + yOffset);
                }

                mHeaderViewVisible = true;
                mGroupPos = groupPosition;
                break;
            }
        }
    }

    /**
     * 根据滚动状态决定如何绘制悬停标题
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //获取该item在listview中的原始position
        // (the raw position of an item (child or group) in the list)
        final long packedPosition = getExpandableListPosition(firstVisibleItem);

        //返回经过包装后的group的position，即当前的group在所有group中的第几个(从0开始)。
        // 参数是getExpandableListPosition()的返回值
        int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

        //返回经过包装后的child的position，即在该group中该child的下标(从0开始)。
        // 如果该group中没有child或者显示的是group界面，那么返回的是-1
        int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

        configureHeaderView(groupPosition, childPosition);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }


    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        super.setAdapter(adapter);

        if (adapter instanceof HoverAdapter) {
            mAdapter = (HoverAdapter) adapter;
        } else {
            throw new RuntimeException("the ExpandableListAdapter must implement HoverAdapter");
        }
    }


    List<View> isDonwIn = new ArrayList<>();
    List<View> isUpIn = new ArrayList<>();
    float downX = -1;
    float downY = -1;
    float upX = -1;
    float upY = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();

                // 只要按下就清空集合信息
                isUpIn.clear();
                isDonwIn.clear();
                for (int i = 0; i < headerClickViews.size(); i++) {
                    View view = headerClickViews.get(i);
                    // 判断触摸点是否在View中
                    if (inArea(ev, view))
                        isDonwIn.add(view);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                super.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_UP:
                upX = ev.getX();
                upY = ev.getY();

                // 如果按下有包含的view才进行循环
                if (isDonwIn.size() > 0) {
                    for (int i = 0; i < headerClickViews.size(); i++) {
                        View view = headerClickViews.get(i);
                        // 判断触摸点是否在View中
                        if (inArea(ev, view))
                            isUpIn.add(view);
                    }
                }
                break;
        }

        // 点下 抬起坐标必须一样 点下 抬起 集合长度都要大于0 并且监听不能为空
        if (downX == upX && downY == upY && isDonwIn.size() > 0 && isUpIn.size() > 0 && listener != null) {

            for (int i = 0; i < isDonwIn.size(); i++) {
                View downView = isDonwIn.get(i);
                if (isUpIn.contains(downView)) {
                    listener.onHoverItemClick(downView.getId(), mGroupPos);
                }
            }

            return true;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    private boolean inArea(MotionEvent event, View mTouchArea) {

        // 控件所占的大小
        Rect rect = new Rect();
        int[] location = new int[2];
        mTouchArea.getLocationInWindow(location);
        rect.left = location[0] + mTouchArea.getLeft();
        rect.top = location[1] + mTouchArea.getTop();
        rect.right = mTouchArea.getWidth() + location[0] + mTouchArea.getLeft();
        rect.bottom = mTouchArea.getHeight() + location[1] + mTouchArea.getTop();
        int left = mTouchArea.getLeft();
        float rawX = event.getX();
        float rawY = event.getY();
        if (rect.contains((int) rawX, (int) rawY)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 悬停 Adapter 接口 . 列表必须实现此接口 .
     */
    public interface HoverAdapter {

        // 当分组没有展开，或者组里没有子项的时候，是不需要绘制悬停标题的
        public static final int PINNED_HEADER_GONE = 0;
        // 显示悬停标题
        public static final int PINNED_HEADER_VISIBLE = 1;
        // 滚动到上一个分组的最后一个子项时，需要把旧的标题“推”出去
        public static final int PINNED_HEADER_PUSHED_UP = 2;

        /**
         * 获取 Header 的状态
         *
         * @param groupPosition
         * @param childPosition
         * @return PINNED_HEADER_GONE, PINNED_HEADER_VISIBLE, PINNED_HEADER_PUSHED_UP 其中之一
         */
        int getHeaderState(int groupPosition, int childPosition);

        /**
         * 更新 Header, 让 Header 知道显示的内容
         *
         * @param header
         * @param groupPosition
         * @param childPosition
         * @param alpha
         */
        void updateHeader(View header, int groupPosition, int childPosition, int alpha);
    }

}

