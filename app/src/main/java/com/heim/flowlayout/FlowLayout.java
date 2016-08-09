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
public class FlowLayout extends ViewGroup {

    //创建行对象
    private Line mLine;

    private int usedWidth;//已用的宽度

    private int mHoriztonlSpacing = 6;//水平间隙宽度

    private int mVerticalSpacing = 6;//垂直间隙宽度


    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //父控件的大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec)-getPaddingLeft()-getPaddingRight();
        int heightSize = MeasureSpec.getSize(heightMeasureSpec)-getPaddingTop()-getPaddingBottom();

        restore();
        //获取布局中子控件的个数
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            //获取子控件
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE) {
                continue;
            }
            //规范子view大小
            int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode ==
                    MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : widthMode);
            int heightSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode ==
                    MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST: heightMode);
            childView.measure(widthSpec, heightSpec); //测量子控件

            if (mLine == null) {
                mLine = new Line();
            }

            int childWidth = childView.getMeasuredWidth();//获取子控件的宽
            usedWidth += childWidth;  //累加已用的宽度

            if (usedWidth <= widthSize) {
                //不换行
                mLine.addView(childView);
                usedWidth += mHoriztonlSpacing;
                if (usedWidth > widthSize) {
                    //换行
                    if (!newLine()) {  //行数超过10,newline会返回false,取反就会进去if,直接break,下面代码不会执行
                        break;
                    }
                }
            } else {
                //换行
                if (!newLine()) {  //换行的方法
                    break;
                }

                mLine.addView(childView);
                usedWidth += childWidth + mHoriztonlSpacing;
            }
        }

        //最后一行不为空,子空间数目大于0,而且行集合中不包含这行
        if (mLine != null && mLine.getCount() > 0 && !lineList.contains(mLine)) {
            lineList.add(mLine);
        }

        int totalLineHeight = 0;
        //流式布局的宽度
        int flowFlayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        for (int i = 0; i < lineList.size(); i++) {
            totalLineHeight += lineList.get(i).lineHeight;
        }

        int totalVerticalSpacing = (lineList.size() - 1) * mVerticalSpacing; //垂直总间隙
        //流式布局的高度
        int flowFlayoutHeight = totalLineHeight + totalVerticalSpacing + getPaddingTop() + getPaddingBottom();
        //给流式布局设置宽高
        setMeasuredDimension(flowFlayoutWidth, flowFlayoutHeight);
    }

    private void restore() {
        lineList.clear();
        mLine = new Line();
        usedWidth=0;
    }

    private List<Line> lineList = new ArrayList<>(); //存储行对象的集合

    private boolean newLine() {
        lineList.add(mLine);//添加行对象
        if (lineList.size() < 10) {  //规定最多10行
            mLine = new Line();     //如果在10行之内,就新建行对象,用来换行
            usedWidth = 0;   //将已用宽度清零
            return true;
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();

        for (int i = 0; i < lineList.size(); i++) {
            Line line = lineList.get(i);
            if (line != null) {
                line.layout(left, top);  //给行进行布局
            }
            top += line.lineHeight + mVerticalSpacing;
        }
    }

    private class Line {
        private List<View> viewList = new ArrayList<>();
        private int lineHeight;
        private int totalLineWidth;

        public void addView(View view) {
            viewList.add(view);
            int viewHeight = view.getMeasuredHeight();
            lineHeight = Math.max(lineHeight, viewHeight);  //当前行最高的高度
            totalLineWidth += view.getMeasuredWidth();
        }

        public int getCount() {
            return viewList.size();
        }

        public void layout(int left, int top) {
            int layoutWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            //留白区域
            int spaceWidth = layoutWidth - totalLineWidth - (getCount() - 1) * mHoriztonlSpacing;
            //平分给每个子控件的宽度
            int extraWidth = spaceWidth / getCount();

            //遍历每一行
            for (int i = 0; i < viewList.size(); i++) {
                View view = viewList.get(i);
                int viewWidth = view.getMeasuredWidth() + extraWidth;
                int viewHeight = view.getMeasuredHeight();

                int viewWidthSpec = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY);
                int viewHeightSpec = MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY);

                view.measure(viewWidthSpec, viewHeightSpec);

                int childTop=(lineHeight - viewHeight) / 2;
                view.layout(left,top+childTop,left+view.getMeasuredWidth(),top+childTop+viewHeight);
                left += view.getMeasuredWidth() + mHoriztonlSpacing;
            }
        }
    }

}
