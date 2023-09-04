package com.example.hazedetect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Range;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class HalfGaugeView extends AppCompatTextView {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float sweepAngle = 270f;
    public float getSweepAngle() {
        return sweepAngle;
    }
    public void setSweepAngle(float value) {
        sweepAngle = value;
    }
    private float lineWidth;
    public float getLineWidth() { return lineWidth; }
    public void setLineWidth(float value) { lineWidth = value; }
    private float dashSize;
    public float getDashSize() { return dashSize; }
    public void setDashSize(float value) { dashSize = value; }
    private int dashColor;
    public int getDashColor() { return dashColor; }
    public void setDashColor(int value) { dashColor = value; }
    private float areaRadius;
    public float getAreaRadius() { return areaRadius; }
    public void setAreaRadius(float value) { areaRadius = value; }
    private int[] colors = new int[2];
    public int[] getColors() { return colors; }
    private Range<Float> dataRange = new Range<>(0f, 100f);
    public Range<Float> getDataRange() { return dataRange; }
    public void setDataRange(Range<Float> value) { dataRange = value; }
    private float gaugeValue = 0f;
    public float getGaugeValue() { return gaugeValue; }
    public void setGaugeValue(float value) { gaugeValue = value; }
    public void setColors(int[] colors) { this.colors = colors; }

    {
        colors[0] = colors[1] = getResources().getColor(R.color.gauge_default_color);
        lineWidth = getResources().getDimension(R.dimen.gauge_line_width);
        dashSize = getResources().getDimension(R.dimen.gauge_dash_size);
        dashColor = getResources().getColor(R.color.gauge_dash_color);
        areaRadius = getResources().getDimension(R.dimen.gauge_area_radius);
    }

    public HalfGaugeView(@NonNull Context context) {
        super(context);
    }

    public HalfGaugeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HalfGaugeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final Rect drawRect = new Rect();
    private final RectF drawRectF = new RectF();

    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 计算绘制区域
        getDrawingRect(drawRect); drawRectF.set(drawRect);
        drawRectF.left += getPaddingLeft();
        drawRectF.top += getPaddingTop();
        drawRectF.right -= getPaddingRight();
        drawRectF.bottom -= getPaddingRight();

        // 先绘制指标值点
        paint.setColor(getDashColor());
        paint.setStyle(Paint.Style.FILL);
        float sweepAngle = getSweepAngle();
        float dashX = drawRectF.centerX(), dashY = drawRectF.centerY();
        Range<Float> range = getDataRange();
        double angle = Math.toRadians(
                sweepAngle * (range.clamp(getGaugeValue()) - range.getLower())
                        / (range.getUpper() - range.getLower())
                - 90. - sweepAngle / 2
        );
        dashX += (float)Math.cos(angle) * drawRectF.width() / 2;
        dashY += (float)Math.sin(angle) * drawRectF.height() / 2;
        canvas.drawCircle(dashX, dashY, getDashSize(), paint);

        // 绘制整个读条，但排除指标值点所在的区域
        canvas.save();
        Path path = new Path();
        path.addCircle(dashX, dashY, getAreaRadius(), Path.Direction.CW);
        canvas.clipPath(path, Region.Op.DIFFERENCE);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(getLineWidth());

        float[] positions = new float[colors.length];
        float start = 0.5f - sweepAngle / 720;
        float sweep = sweepAngle / 360;
        for (int i = 0; i < colors.length; ++i)
            positions[i] = start + sweep * i / (colors.length - 1.0f);

        Matrix matrix = new Matrix();
        matrix.setTranslate(drawRectF.centerX(), drawRectF.centerY());
        matrix.preRotate(90);
        SweepGradient grad = new SweepGradient(0, 0, colors, positions);
        grad.setLocalMatrix(matrix);

        paint.setShader(grad);
        canvas.drawArc(drawRectF, 270f - sweepAngle / 2, sweepAngle, false, paint);
        paint.setShader(null);

        canvas.restore();
    }
}
