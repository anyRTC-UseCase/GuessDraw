package io.anyrtc.drawsomething.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import io.anyrtc.drawsomething.R;

public class RoundBackgroundView extends View {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private final Path mPath = new Path();

    private float borderRadius;
    private float strokeWidth;
    private float halfBorderWidth;
    private float halfBorderPadding;
    private int borderColor;
    private int backgroundColor;

    public RoundBackgroundView(Context context) {
        this(context, null);
    }

    public RoundBackgroundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int color;
        if (attrs != null) {
            TypedArray typedArray = context.getResources()
                    .obtainAttributes(attrs, R.styleable.RoundBackgroundView);
            color = typedArray.getColor(
                    R.styleable.RoundBackgroundView_round_background_color, Color.RED
            );
            borderRadius = typedArray.getDimension(
                    R.styleable.RoundBackgroundView_round_radius, 20.0f
            );
            strokeWidth = typedArray.getDimension(
                    R.styleable.RoundBackgroundView_round_border_width, 0.0f
            );
            borderColor = typedArray.getColor(
                    R.styleable.RoundBackgroundView_round_border_color, Color.TRANSPARENT
            );
            float borderPadding = typedArray.getDimension(
                    R.styleable.RoundBackgroundView_round_border_padding, 0.0f
            );
            halfBorderWidth = strokeWidth / 2.0f;
            halfBorderPadding = borderPadding / 2.0f;
            typedArray.recycle();
        } else {
            color = Color.RED;
            borderRadius = 20.0f;
        }
        backgroundColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mPath.reset();
        rectF.set(0.0f, 0.0f, width, height);

        mPath.addRoundRect(rectF, borderRadius, borderRadius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(mPath);
        final float mTop = strokeWidth + 0.0f;
        rectF.set(strokeWidth, mTop, width - strokeWidth, height - strokeWidth);

        canvas.drawColor(Color.WHITE);
        if (strokeWidth != 0.0f) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);
            mPaint.setColor(borderColor);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            canvas.drawRoundRect(rectF, borderRadius, borderRadius, mPaint);
        }

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0);
        mPaint.setColor(backgroundColor);
        rectF.set(
                strokeWidth + halfBorderWidth + halfBorderPadding,
                strokeWidth + halfBorderWidth + halfBorderPadding,
                width - halfBorderWidth - halfBorderPadding - strokeWidth,
                height - halfBorderWidth - halfBorderPadding - strokeWidth
        );
        final float newRadius = Math.max(borderRadius - halfBorderWidth - halfBorderPadding, 0.0f);
        canvas.drawRoundRect(rectF, newRadius, newRadius, mPaint);
        canvas.restore();
    }

    public void setBackgroundColor(@ColorInt int color) {
        this.backgroundColor = color;
        invalidate();
    }

    public int getBackgroundColor() {
        return this.backgroundColor;
    }
}