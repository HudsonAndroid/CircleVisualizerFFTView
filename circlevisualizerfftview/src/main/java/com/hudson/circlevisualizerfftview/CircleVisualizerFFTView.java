package com.hudson.circlevisualizerfftview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by Hudson on 2017/2/27.
 * 主要目的：熟悉canvas的变化
 * 实现思路：超级简单，仅一个api,即canvas.rotate。
 */

public class CircleVisualizerFFTView extends View {
    private static final int DEFAULT_CAKE_DEGREE = 5;//默认的频谱块旋转角度。canvas就是通过它旋转的
    private static final int DEFAULT_CAKE_COLOR = Color.WHITE;//默认的频谱块颜色
    private static final int DEFAULT_PADDING_OFFSET = 60;//dp。默认的控件外围预留区域
    private int[] mHeights;//频谱块高度数组
    private int mCakeCount;//频谱块个数
    private int mCakeDegree;
    private int mCakeColor;
    private int mCircleRadius;
    private int mPaddingOffset;
    private Paint mPaint;
    private int mCenterX;
    private int mCenterY;
    private int mDrawStartY;

    public CircleVisualizerFFTView(Context context) {
        this(context,null);
    }

    public CircleVisualizerFFTView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircleVisualizerFFTView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs) {
        if(attrs!=null){//避免由于第一个构造方法造成异常
            TypedArray ta = context.obtainStyledAttributes(attrs,
                    R.styleable.CircleVisualizerFFTView);
            mCakeDegree = ta.getInteger(R.styleable.CircleVisualizerFFTView_cake_degree,DEFAULT_CAKE_DEGREE);
            mCakeColor = ta.getColor(R.styleable.CircleVisualizerFFTView_cake_color,DEFAULT_CAKE_COLOR);
            mPaddingOffset = (int) dp2px(ta.getDimension(R.styleable.CircleVisualizerFFTView_padding_offset,DEFAULT_PADDING_OFFSET),context);
            ta.recycle();
        }else {
            mCakeDegree = DEFAULT_CAKE_DEGREE;
            mCakeColor = DEFAULT_CAKE_COLOR;
            mPaddingOffset = DEFAULT_PADDING_OFFSET;
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mCakeColor);
        mPaint.setStrokeWidth(10);
        mCakeCount = 360 / mCakeDegree;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(MeasureSize(width, widthMode,1),
                MeasureSize(height, heightMode,2));//或者使用super.onMeasure()
    }

    /**
     * 测量控件大小
     * @param size
     * @param sizeMode
     * @param code 1表示width，2表示height
     * @return
     */
    private int MeasureSize(int size, int sizeMode,int code) {
        if (sizeMode == MeasureSpec.EXACTLY) {// 如果指定了明确的大小
            return size;
        } else {// 根据我们的情况设置大小
            int requireSize = 0;
            if(code == 1){//表示width,200是默认半径值,像素
                requireSize = getPaddingLeft()+getPaddingRight()+(200+mPaddingOffset)*2;
            }else if(code == 2){//表示height
                requireSize = getPaddingBottom()+getPaddingTop()+(200+mPaddingOffset)*2;
            }
            if (sizeMode == MeasureSpec.AT_MOST) {
                requireSize = Math.min(size, requireSize);
            }
            return requireSize;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(w<h){
            mCircleRadius = (w - getPaddingLeft() -getPaddingRight() - mPaddingOffset*2)/2;
        }else{
            mCircleRadius = (h - getPaddingBottom() - getPaddingTop() - mPaddingOffset*2)/2;
        }
        mCenterX = w/2;
        mCenterY = h/2;
        mDrawStartY = mCenterY - mCircleRadius;


        super.onSizeChanged(w, h, oldw, oldh);
    }

    //初始化频谱块高度
    public void updateVisualizer(byte[] fft) {
        int[] model = new int[fft.length / 2 + 1];
        model[0] =  Math.abs(fft[0]);
        for (int i = 2, j = 1; j < mCakeCount;) {
            model[j] = (int) Math.hypot(fft[i], fft[i + 1]);
            i += 2;
            j++;
        }
        mHeights = model;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mHeights == null) {
            return;
        }
        drawLine(canvas, mHeights[0]);
        for (int i = 0; i < mCakeCount; i++) {
            canvas.rotate(mCakeDegree,mCenterX,mCenterY);
            drawLine(canvas, mHeights[i+1]);
        }
    }

    public void drawLine(Canvas canvas,int height){
        mPaint.setAlpha(255);
        if(height>mDrawStartY){
            canvas.drawLine(mCenterX,mDrawStartY,mCenterX,50,mPaint);
        }else{
            canvas.drawLine(mCenterX,mDrawStartY,mCenterX,mDrawStartY-height,mPaint);
        }
        mPaint.setAlpha(150);
        canvas.drawLine(mCenterX,mDrawStartY+10,mCenterX,mDrawStartY+10+height*0.4f,mPaint);
    }

    public static int dp2px(int dp, Context context) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics())+0.5f);
    }

    public static float dp2px(float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}
