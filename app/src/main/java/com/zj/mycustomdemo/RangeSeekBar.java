package com.zj.mycustomdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zj on 2018/10/15 9:17.
 * 主要作用： 1.画出区间条 2. 滑块 3.背景条形图
 */
public class RangeSeekBar extends View {

    /**
     * 定义3中状态 -》 在左控件，在右控件，不在控件上
     */

    public static final String RSB_LEFT = "rsb_left";

    public static final String RSB_RIGHT = "rsb_right";

    public static final String RSB_NO = "rsb_no";

    private float   mLineHeight;
    private int     mRsbRadius;
    private float   mLineMargin;
    private float   mLineLeft;
    private float   mLineTop;
    private float   mLineBottom;
    private float   mLineRight;
    private Paint   mLinePaint;
    private int     mSbIconId;
    private float   mSbIconHw;
    private float   mLineWidth;
    private String  mLineMax;
    private String  mLineMin;
    private int     mMin;
    private int     mMax;
    private float   mLineTvSize;
    private int     mLineMode;
    private int     mLineReverse;
    private float   mTvLineMarginTop;
    private Paint   mTvPaint;
    private Paint   mDefaultPaint;
    private SeekBar leftSeekBar;
    private float   sbRadius;
    private SeekBar rightSeekBar;
    private Bitmap mProgressHintBg;

    public RangeSeekBar(Context context) {
        this(context, null);
    }

    public RangeSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);
        mLineHeight = ta.getDimension(R.styleable.RangeSeekBar_line_height, dp2px(context, 10)); // line的高度
        mLineMargin = ta.getDimension(R.styleable.RangeSeekBar_line_margin, dp2px(context, 10)); // line 左右两边的间隔
        mLineMax = ta.getString(R.styleable.RangeSeekBar_line_max); // 最大值
        mLineMin = ta.getString(R.styleable.RangeSeekBar_line_min); // 最小值
        mLineTvSize = ta.getDimension(R.styleable.RangeSeekBar_line_tv_size, sp2px(context, 20));
        mLineMode = ta.getInteger(R.styleable.RangeSeekBar_line_mode, 5);// 默认为5等份
        mLineReverse = ta.getInt(R.styleable.RangeSeekBar_line_two_reverse, 1);
        mTvLineMarginTop = ta.getDimension(R.styleable.RangeSeekBar_line_tv_margin_top, dp2px(context, 10)); // 标尺距离滑块的距离


        mSbIconId = ta.getResourceId(R.styleable.RangeSeekBar_sb_icon_id, 0); // 滑块的资源id
        mSbIconHw = ta.getDimension(R.styleable.RangeSeekBar_sb_icon_hw, dp2px(context, 10));//滑块的宽高，默认为一样大

        initRule(mLineMin, mLineMax);

        ta.recycle();

        leftSeekBar = new SeekBar();

        rightSeekBar = new SeekBar();

        initPaint();
    }

    /*指定规则，防止程序发生错误*/
    public void initRule(String lineMin, String lineMax) {
        if (TextUtils.isEmpty(lineMin)) {
            lineMin = "0";
        }

        if (lineMax.contains("+")) {
            lineMax = lineMax.replace("+", "");
        }

        mMin = Integer.parseInt(lineMin);

        mMax = Integer.parseInt(lineMax);

        if (mMax <= mMin) {

            throw new IllegalArgumentException("line_max must bigger than line_min!!!");
        }
    }

    private void initPaint() {

        // 区间条的
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.parseColor("#d9d9d9"));
        mLinePaint.setStyle(Paint.Style.FILL);

        // 标尺
        mTvPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTvPaint.setStyle(Paint.Style.FILL);
        mTvPaint.setColor(Color.BLACK);
        mTvPaint.setTextSize(mLineTvSize);

        // 滑块
        mDefaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 这里就判断如果高大于宽就选择用宽的高度
        if (heightSize > widthSize) {
            setMeasuredDimension(widthSize, widthSize);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 1.先画出区间条，默认在竖直中间画 todo 以后需要调整
        mRsbRadius = h / 2;
        mLineLeft = mLineMargin;
        mLineTop = mRsbRadius - mLineHeight / 2;
        mLineBottom = mRsbRadius + mLineHeight / 2;
        mLineRight = w - mLineMargin;
        mLineWidth = mLineRight - mLineLeft;

        leftSeekBar.setValue(mMin);
        rightSeekBar.setValue(mMax);

        leftSeekBar.sizeChange(getContext());

        rightSeekBar.sizeChange(getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 1. 画出区间条
        RectF lineRectF = new RectF(mLineLeft, mLineTop, mLineRight, mLineBottom);
        canvas.drawRoundRect(lineRectF, mLineHeight / 2, mLineHeight / 2, mLinePaint);

        //画出区间选中区域
        drawLineSelected(canvas);
        // 2.画出滑块
        leftSeekBar.draw(canvas);
        rightSeekBar.draw(canvas);

    }

    private void drawLineSelected(Canvas canvas) {

        mDefaultPaint.setStyle(Paint.Style.FILL);

        mDefaultPaint.setColor(Color.parseColor("#4bd962"));

        float startx = mLineWidth * leftSeekBar.currentPercent + mLineMargin;

        float endX = mLineMargin + rightSeekBar.currentPercent * mLineWidth;

        RectF rectF = new RectF(startx, mLineTop, endX, mLineBottom);

        canvas.drawRoundRect(rectF, mLineHeight / 2, mLineHeight / 2, mDefaultPaint);

    }

    /**
     * 由于滑块有多种属性，如大小，颜色，滑动等，所以单独的定义个类
     */

    class SeekBar {

        private float sbLeft, sbTop, sbRight, sbBottom;

        private float sbWidth, sbHeight;

        private float currentPercent;

        private Bitmap bmp;

        public void setValue(float currentData) {

            if (currentData > mMax || currentData < mMin) {

                throw new IllegalArgumentException("currentData must bigger min or less than max !!!");
            }

            currentPercent = (currentData - mMin) / (mMax - mMin);
        }

        void sizeChange(Context context) {

            sbWidth = mSbIconHw;
            sbHeight = mSbIconHw;
            sbRadius = sbWidth / 2;

            sbLeft = mLineMargin - sbRadius;
            sbTop = mRsbRadius - sbRadius;
            sbRight = sbLeft + sbWidth;
            sbBottom = sbTop + sbHeight;

            if (mSbIconId > 0) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), mSbIconId);
                if (bitmap != null) {
                    int height = bitmap.getHeight();
                    int width = bitmap.getWidth();
                    float widthScale = sbWidth / width;
                    float heightScale = sbHeight / height;
                    Matrix matrix = new Matrix();
                    matrix.postScale(widthScale, heightScale);
                    bmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                }
            }
        }

        void draw(Canvas canvas) {

            float offset = mLineWidth * currentPercent;

            // 画出区间
            drawLineText(canvas);

            // 画出上面部分的数字标签
            drawLineIconTv(canvas);

            canvas.save();

            canvas.translate(offset, 0);

            if (bmp != null) { // 使用icon

                canvas.drawBitmap(bmp, sbLeft, sbTop, null);

            } else { //自定义图形

                mDefaultPaint.setStyle(Paint.Style.FILL);

                canvas.save();

                canvas.translate(0, sbRadius * 0.18f);

                canvas.scale(1, 1, sbLeft + sbRadius, mRsbRadius);

                RadialGradient radialGradient = new RadialGradient(sbLeft + sbRadius, mRsbRadius, sbRadius * 0.95f, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);

                mDefaultPaint.setShader(radialGradient);

                canvas.drawCircle(sbLeft + sbRadius, mRsbRadius, sbRadius, mDefaultPaint);

                mDefaultPaint.setShader(null);

                canvas.restore();

                mDefaultPaint.setColor(0xFFFFFFFF);

                canvas.drawCircle(sbLeft + sbRadius, mRsbRadius, sbRadius - 3, mDefaultPaint);

            }

            canvas.restore();
        }


        private void drawLineIconTv(Canvas canvas) {

            mProgressHintBg = BitmapFactory.decodeResource(getResources(), R.drawable.progress_hint_bg3);

            RectF rectF = new RectF();


        }

        public String getCurrentRange() {

            float leftData = leftSeekBar.currentPercent * (mMax - mMin) + mMin;

            float rightData = rightSeekBar.currentPercent * (mMax - mMin) + mMin;

            return leftData + "," + rightData;
        }

        private void drawLineText(Canvas canvas) {

            int temp = (mMax - mMin) / mLineMode;

            float tempX = mLineWidth / mLineMode;

            float currentX;

            String currentData; // 先是默认显示最小值

            for (int i = 0; i < mLineMode + 1; i++) {

                if (i == mLineMode) {
                    currentData = mMax + "+";
                    currentX = mLineRight;
                } else {
                    currentData = mMin + temp * i + "";
                    currentX = mLineLeft + tempX * i;
                }

                float tvLength = mTvPaint.measureText(currentData + "");

                canvas.drawText(currentData + "", currentX - tvLength * 2 / 3, mRsbRadius + sbRadius + mTvLineMarginTop, mTvPaint);
            }
        }

        /**
         * 判断是否点击在seekbar上
         */
        private String collide(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            float sbRightLeft = rightSeekBar.sbLeft + mLineWidth * rightSeekBar.currentPercent;
            float sbLeftLeft = leftSeekBar.sbLeft + mLineWidth * leftSeekBar.currentPercent;

            if (y >= mRsbRadius - sbRadius && y <= mRsbRadius + sbRadius) {
                if (x >= sbRightLeft && x <= sbRightLeft + sbRadius * 2 && x >= sbLeftLeft && x <= sbLeftLeft + sbRadius * 2) {
                    if (x > mLineLeft + mLineWidth / 2) {
                        return RSB_LEFT;
                    } else {
                        return RSB_RIGHT;
                    }
                } else if (x >= sbRightLeft && x <= sbRightLeft + sbRadius * 2) {
                    return RSB_RIGHT;
                } else if (x >= sbLeftLeft && x <= sbLeftLeft + sbRadius * 2) {
                    return RSB_LEFT;
                } else {
                    return RSB_NO;
                }
            } else {
                return RSB_NO;
            }
        }

        void slide(float percent) {
            if (percent >= 1) {
                percent = 1;
            } else if (percent < 0) {
                percent = 0;
            }
            currentPercent = percent;

        }
    }

    private SeekBar touchMode;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                boolean touchResult = false;

                String seekbarFlag = rightSeekBar.collide(event);

                switch (seekbarFlag) {

                    case RSB_LEFT:
                        touchResult = true;
                        touchMode = leftSeekBar;
                        break;

                    case RSB_RIGHT:
                        touchResult = true;
                        touchMode = rightSeekBar;
                        break;

                    case RSB_NO:
                        touchMode = null;
                        break;
                }

                return touchResult;

            case MotionEvent.ACTION_MOVE:

                if (touchMode != null) {

                    float endX = event.getX();

                    float temp = (mLineReverse * 1.0f / (mMax - mMin)) * mLineWidth;

                    float sbRightLeft = rightSeekBar.sbLeft + mLineWidth * rightSeekBar.currentPercent;

                    float sbLeftLeft = leftSeekBar.sbLeft + mLineWidth * leftSeekBar.currentPercent;

                    if (touchMode == leftSeekBar) {

                        if (endX > sbRightLeft + sbRadius - temp) {

                            endX = sbRightLeft + sbRadius - temp;
                        }

                    } else if (touchMode == rightSeekBar) {

                        if (endX < sbLeftLeft + sbRadius + temp) {
                            endX = sbLeftLeft + sbRadius + temp;
                        }
                    }

                    float percent = (endX - mLineMargin) / mLineWidth;

                    if (percent >= 1) {
                        percent = 1;
                    } else if (percent <= 0) {
                        percent = 0;
                    }

                    touchMode.slide(percent);
                    invalidate();
                }

                break;

            case MotionEvent.ACTION_UP:

                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 将dp转成px
     */
    public int dp2px(Context context, int dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
