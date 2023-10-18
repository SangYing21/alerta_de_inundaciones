package com.example.alerta_de_inundaciones;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class WaveView extends View {
    private float mCurrentWaveHeight = 0;
    private Path mWavePath;
    private Paint mWavePaint;
    private float mWaveHeight;
    private float mWaveLength;
    private float mWaveOffset = 0;
    private float mWaveSpeed = 0.1f;


    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        mWaveHeight = a.getDimension(R.styleable.WaveView_waveHeight, 0);
        a.recycle();
    }

    public float getWaveHeight() {
        return mWaveHeight;
    }

    public void setWaveHeight(float waveHeight) {
        ValueAnimator animator = ValueAnimator.ofFloat(mWaveHeight, waveHeight);
        animator.setDuration(500); // duración de la animación en milisegundos
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWaveHeight = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    private void init() {
        int[] colors = { Color.rgb(66, 159, 235), Color.rgb(36, 124, 224), Color.rgb(4, 35, 220)};
        float[] positions = {0f, 0.5f, 1f}; //opcional, si quieres establecer las posiciones de los colores en el gradiente
        Shader gradient = new LinearGradient(0, 0, 0, 700, colors, positions, Shader.TileMode.CLAMP);
        mWavePath = new Path();
        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWavePaint.setShader(gradient);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWaveLength = 100; // longitud de la onda en píxeles
        mWaveOffset = 0; // desplazamiento inicial de la onda
        startAnimation(); // comenzar la animación
    }

    private void startAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(8000); // duración de la animación en milisegundos
        animator.setRepeatCount(ValueAnimator.INFINITE); // repetir la animación indefinidamente
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWaveOffset = mWaveLength * (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float halfWidth = width / 10.0f;
        float halfHeight = height - (mWaveHeight / 29.0f) * height;

        mWavePath.reset();

        mWavePath.moveTo(0, halfHeight);

        for (float x = 0; x <= width; x += 20) {
            float y = halfHeight + (float) Math.sin(x * 2 * Math.PI / width) * mWaveHeight;
            mWavePath.lineTo(x, y);
        }

        mWavePath.lineTo(width, height);
        mWavePath.lineTo(0, height);
        mWavePath.close();

        canvas.drawPath(mWavePath, mWavePaint);
    }
}