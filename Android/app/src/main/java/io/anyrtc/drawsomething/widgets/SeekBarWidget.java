package io.anyrtc.drawsomething.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import io.anyrtc.drawsomething.R;


public class SeekBarWidget extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float circleRadius;
    private final float circleStrokeWidth;
    private final float lineHeight;
    private float percentage;
    private int progress;
    private int beforeProgress;
    private final int maxProgress;
    private final int minProgress;
    private ColorTransition colorTransition;
    private int backgroundColor;
    private int circleStrokeColor;
    private final float horizontalPadding;
    private final float mPaddingLeft;

    private OnProgressChangeListener mListener;

    public SeekBarWidget(Context context) {
        this(context, null);
    }

    public SeekBarWidget(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (null != attrs) {
            TypedArray typedArray = getResources().obtainAttributes(attrs, R.styleable.SeekBarWidget);
            minProgress = typedArray.getInt(R.styleable.SeekBarWidget_seek_minProgress, 0);
            maxProgress = typedArray.getInt(R.styleable.SeekBarWidget_seek_maxProgress, 100) - minProgress;
            progress = typedArray.getInt(R.styleable.SeekBarWidget_seek_progress, 0) - minProgress;
            if (progress < 0) progress = minProgress;
            circleRadius = typedArray.getDimension(R.styleable.SeekBarWidget_seek_circleRadius, 20f);
            circleStrokeWidth = typedArray.getDimension(R.styleable.SeekBarWidget_seek_circleStrokeWidth, 5f);
            lineHeight = typedArray.getDimension(R.styleable.SeekBarWidget_seek_lineHeight, 5f);
            backgroundColor = typedArray.getColor(R.styleable.SeekBarWidget_seek_backgroundColor, Color.parseColor("#F0F0F0"));
            circleStrokeColor = typedArray.getColor(R.styleable.SeekBarWidget_seek_circleStrokeColor, Color.WHITE);
            int maxColor = typedArray.getColor(R.styleable.SeekBarWidget_seek_maxColor, Color.RED);
            int startColor = typedArray.getColor(R.styleable.SeekBarWidget_seek_startColor, maxColor);
            colorTransition = new ColorTransition(startColor, maxColor);
            typedArray.recycle();
            percentage = progress * 1.0f / maxProgress;
            horizontalPadding = circleRadius * 2 + circleStrokeWidth * 2 + getPaddingStart() + getPaddingEnd();
            mPaddingLeft = horizontalPadding - getPaddingEnd() - circleRadius - circleStrokeWidth;
            return;
        }
        maxProgress = 100;
        minProgress = 0;
        circleRadius = 20;
        circleStrokeWidth = 5;
        lineHeight = 5;
        backgroundColor = Color.parseColor("#F0F0F0");
        colorTransition = new ColorTransition(Color.WHITE, Color.RED);
        circleStrokeColor = Color.WHITE;

        horizontalPadding = circleRadius * 2 + circleStrokeWidth * 2 + getPaddingStart() + getPaddingEnd();
        mPaddingLeft = horizontalPadding - getPaddingEnd() - circleRadius - circleStrokeWidth;
    }

    @SuppressWarnings("ALL")
    @Override
    protected void onDraw(Canvas canvas) {
        final int width = (int) (getMeasuredWidth() - horizontalPadding);
        final int height = getMeasuredHeight();

        paint.setColor(backgroundColor);
        paint.setStrokeWidth(lineHeight);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(mPaddingLeft, height >> 1, mPaddingLeft + width, height >> 1, paint);

        //float percentage = progress / maxProgress;
        int currColor = colorTransition.getValue(percentage);
        paint.setColor(currColor);
        canvas.drawLine(mPaddingLeft, height >> 1, mPaddingLeft + width * percentage, height >> 1, paint);

        // draw circle border
        paint.setColor(circleStrokeColor);
        canvas.drawCircle(mPaddingLeft + width * percentage, height >> 1, circleRadius + (circleStrokeWidth / 2f), paint);
        // draw circle inside color
        paint.setColor(currColor);
        canvas.drawCircle(mPaddingLeft + width * percentage, height >> 1, circleRadius, paint);
    }

    private float downX;
    private float downY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean intercept = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                intercept = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                float xMove = Math.abs(moveX - downX) - Math.abs(moveY - downY);
                if (xMove > 0f) {
                    float hX = moveX - downX;
                    boolean toLeft = hX < 0.0f;
                    float movePercent = Math.abs(hX) / getMeasuredWidth();
                    if (percentage < 1.0f && !toLeft) {
                        percentage += movePercent;
                    } else if (percentage > 0f && toLeft) {
                        percentage -= movePercent;
                    }

                    if (percentage < 0f) percentage = 0f;
                    if (percentage > 1f) percentage = 1f;

                    progress = (int) Math.floor(percentage * maxProgress);
                }
                intercept = true;
                downX = moveX;
                downY = moveY;
                postInvalidate();

                if (null != mListener && beforeProgress != getProgress()) {
                    beforeProgress = getProgress();
                    mListener.onProgress(getProgress());
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return intercept || super.onTouchEvent(event);
        //return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    public int getProgress() {
        return this.progress + minProgress;
    }
    public void setProgress(int progress) {
        this.progress = progress - minProgress;
		this.percentage = this.progress * 1.0f / maxProgress;
        postInvalidate();
    }

    public void setOnProgressChangListener(OnProgressChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnProgressChangeListener {
        void onProgress(int progress);
    }

	public void setProgressBackgroundColor(int color) {
		this.backgroundColor = color;
		invalidate();
	}

	public void setProgressForegroundColor(int beginColor, int endColor) {
		this.colorTransition = new ColorTransition(beginColor, endColor);
		invalidate();
	}

	public void setProgressStrokeColor(int color) {
		this.circleStrokeColor = color;
		invalidate();
	}

    public static class ColorTransition
    {
        private final int fromColor;
        private final int toColor;

        public ColorTransition(int beginColor, int endColor)
        {
            this.fromColor = beginColor;
            this.toColor = endColor;
        }

        public int getValue(float percentage)
        {
            int fromA = Color.alpha(fromColor);
            int fromR = Color.red(fromColor);
            int fromG = Color.green(fromColor);
            int fromB = Color.blue(fromColor);

            int toA = Color.alpha(toColor);
            int toR = Color.red(toColor);
            int toG = Color.green(toColor);
            int toB = Color.blue(toColor);

            return Color.argb(
                    (int) (fromA + ((toA - fromA) * percentage)),
                    (int) (fromR + ((toR - fromR) * percentage)),
                    (int) (fromG + ((toG - fromG) * percentage)),
                    (int) (fromB + ((toB - fromB) * percentage))
            );
        }
    }
}
