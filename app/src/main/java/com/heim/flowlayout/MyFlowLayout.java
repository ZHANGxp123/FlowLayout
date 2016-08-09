package com.heim.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxp on 2016/7/14 0014.
 */
public class MyFlowLayout extends ViewGroup {

    private int  usedWidth;
    private Line mLine;
    private int mHoriztonalSpacing = 6;
    private int mVerticalSpacing   = 6;
    private List<Line> lineList = new ArrayList<>();//存储行对象的集合


    public MyFlowLayout(Context context) {
        super(context);
    }

    public MyFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //获得父控件的大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight() - getPaddingLeft();
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();

        reset();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE) {
                continue;
            }
            //规定子控件的测量方式
            int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : widthMode);
            int heightSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : heightMode);

            childView.measure(widthSpec, heightSpec);

            if (mLine == null) {
                mLine = new Line();
            }

            int childWidth = childView.getMeasuredWidth();
            usedWidth += childWidth;  //累加已用水平空间
            if (usedWidth <= widthSize) {
                //不换行
                mLine.addView(childView);
                usedWidth += mHoriztonalSpacing;
                if (usedWidth > widthSize) { //当一行中累加了几个view之后,大于父控件的宽度,需要换行
                    if (!newLine()){
                        break;
                    }
                }

            } else {
                //换行
                if (!newLine()){
                    break;
                }
                mLine.addView(childView);//添加子view
                usedWidth += childWidth + mHoriztonalSpacing;
            }
        }

        //最后一行
        if (mLine != null && mLine.getCount() > 0 && !lineList.contains(mLine)) {
            lineList.add(mLine);
        }

        int totalLineHeight=0;
        //流式布局的宽度
        int layoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        for (int i = 0; i < lineList.size(); i++) {
            totalLineHeight += lineList.get(i).lineHeight;
        }

        int totalVerticalSpacing = (lineList.size() - 1) * mVerticalSpacing;//垂直总间隙
        //流式布局的高度
        int layoutHeight = totalLineHeight + totalVerticalSpacing + getPaddingBottom() + getPaddingTop();
        //将宽高设置给布局
        setMeasuredDimension(layoutWidth, layoutHeight);
    }

    private boolean newLine() {
        lineList.add(mLine);
        if (lineList.size() < 10) {
            mLine = new Line();//新创建一行
            usedWidth=0;
            return true;
        }
        return false;
    }

    private void reset() {
        lineList.clear();
        mLine = new Line();
        usedWidth=0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int i = 0; i < lineList.size(); i++) {
            Line line = lineList.get(i);
            line.layout(left, top);
            top += line.lineHeight + mVerticalSpacing;
        }
    }

    private class Line {

        private List<View> viewList = new ArrayList<>();
        private int lineHeight; //行高
        private int totalWidth;
        public void addView(View view) {
            viewList.add(view);
            int viewHeight = view.getMeasuredHeight();
            lineHeight = Math.max(lineHeight, viewHeight); //区两者中较大者
            totalWidth += view.getMeasuredWidth();  //一行中控件总宽度
        }

        public int getCount() {
            return viewList.size();
        }

        public void layout(int left, int top) {
            int width = getMeasuredWidth() - getPaddingRight() - getPaddingLeft();
            //空白区域宽度
            int spaceWidth = width - totalWidth - (getCount() - 1) * mHoriztonalSpacing;
            //平分到每个子控件上的宽度
            int extraWidth = spaceWidth / getCount();
            for (int i = 0; i < viewList.size(); i++) {
                View view = viewList.get(i);
                int viewWidth = view.getMeasuredWidth() + extraWidth;  //每个子view的大小
                int viewHeight = view.getMeasuredHeight();

                view.measure(viewWidth, viewHeight);//重新测量
                int childTop = (lineHeight - viewHeight) / 2;
                view.layout(left,top+childTop,left+viewWidth,top+childTop+viewHeight);
                left += mHoriztonalSpacing + viewWidth;
            }
        }
    }

}
