/*
 * 	   Created by Daniel Nadeau
 * 	   daniel.nadeau01@gmail.com
 * 	   danielnadeau.blogspot.com
 *
 * 	   Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.echo.holographlibrary;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

public class PieGraph extends View implements  HoloGraphAnimate {

    private int mPadding;
    private int mInnerCircleRatio;
    private ArrayList<PieSlice> mSlices = new ArrayList<PieSlice>();
    private Paint mPaint = new Paint();
    private int mSelectedIndex = -1;
    private OnSliceClickedListener mListener;
    private boolean mDrawCompleted = false;
    private RectF mRectF = new RectF();
    private Bitmap mBackgroundImage = null;
    private Point mBackgroundImageAnchor = new Point(0,0);
    private boolean mBackgroundImageCenter = false;



    private int mDuration = 300;//in ms
    private Interpolator mInterpolator;
    private Animator.AnimatorListener mAnimationListener;
    private ValueAnimator mValueAnimator;

    public PieGraph(Context context) {
        this(context, null);
    }

    public PieGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PieGraph, 0, 0);
        mInnerCircleRatio = a.getInt(R.styleable.PieGraph_pieInnerCircleRatio, 0);
        mPadding = a.getDimensionPixelSize(R.styleable.PieGraph_pieSlicePadding, 0);
    }

    public void onDraw(Canvas canvas) {
        float midX, midY, radius, innerRadius;

        canvas.drawColor(Color.TRANSPARENT);
        mPaint.reset();
        mPaint.setAntiAlias(true);

        if(mBackgroundImage != null) {
            if(mBackgroundImageCenter)
                mBackgroundImageAnchor.set(
                        getWidth() / 2 - mBackgroundImage.getWidth() / 2,
                        getHeight() / 2 - mBackgroundImage.getHeight() / 2
                );
            canvas.drawBitmap(mBackgroundImage, mBackgroundImageAnchor.x, mBackgroundImageAnchor.y, mPaint);
        }

        float currentAngle = 270;
        float currentSweep = 0;
        float totalValue = 0;

        midX = getWidth() / 2;
        midY = getHeight() / 2;
        if (midX < midY) {
            radius = midX;
        } else {
            radius = midY;
        }
        radius -= mPadding;
        innerRadius = radius * mInnerCircleRatio / 255;

        for (PieSlice slice : mSlices) {
            totalValue += slice.getValue();
        }

        int count = 0;
        for (PieSlice slice : mSlices) {
            Path p = slice.getPath();
            p.reset();

            if (mSelectedIndex == count && mListener != null) {
                mPaint.setColor(slice.getSelectedColor());
            } else {
                mPaint.setColor(slice.getColor());
            }
            currentSweep = (slice.getValue() / totalValue) * (360);

            mRectF.set(midX - radius, midY - radius, midX + radius, midY + radius);
            createArc(p, mRectF, currentSweep,
                    currentAngle + mPadding, currentSweep - mPadding);
            mRectF.set(midX - innerRadius, midY - innerRadius,
                    midX + innerRadius, midY + innerRadius);
            createArc(p, mRectF, currentSweep,
                    (currentAngle + mPadding) + (currentSweep - mPadding),
                    -(currentSweep - mPadding));

            p.close();

            // Create selection region
            Region r = slice.getRegion();
            r.set((int) (midX - radius),
                    (int) (midY - radius),
                    (int) (midX + radius),
                    (int) (midY + radius));
            canvas.drawPath(p, mPaint);
            currentAngle = currentAngle + currentSweep;

            count++;
        }
        mDrawCompleted = true;
    }

    private void createArc(Path p, RectF mRectF, float currentSweep, float startAngle, float sweepAngle) {
        if (currentSweep == 360) {
            p.addArc(mRectF, startAngle, sweepAngle);
        } else {
            p.arcTo(mRectF, startAngle, sweepAngle);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDrawCompleted) {
            Point point = new Point();
            point.x = (int) event.getX();
            point.y = (int) event.getY();

            int count = 0;
            Region r = new Region();
            for (PieSlice slice : mSlices) {
                r.setPath(slice.getPath(), slice.getRegion());
                switch (event.getAction()) {
                    default:
                        break;
                    case MotionEvent.ACTION_DOWN:
                        if (r.contains(point.x, point.y)) {
                            mSelectedIndex = count;
                            postInvalidate();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (count == mSelectedIndex
                                && mListener != null
                                && r.contains(point.x, point.y)) {
                            mListener.onClick(mSelectedIndex);
                        }
                        break;
                }
                count++;
            }
        }
        // Case we click somewhere else, also get feedback!
        if(MotionEvent.ACTION_UP == event.getAction()
                && mSelectedIndex == -1
                && mListener != null) {
            mListener.onClick(mSelectedIndex);
        }
        // Reset selection
        if (MotionEvent.ACTION_UP == event.getAction()
                || MotionEvent.ACTION_CANCEL == event.getAction()) {
            mSelectedIndex = -1;
            postInvalidate();
        }
        return true;
    }

    public Bitmap getBackgroundBitmap() {
        return mBackgroundImage;
    }

    public void setBackgroundBitmap(Bitmap backgroundBitmap, int pos_x, int pos_y) {
        mBackgroundImage = backgroundBitmap;
        mBackgroundImageAnchor.set(pos_x, pos_y);
        postInvalidate();
    }

    public void setBackgroundBitmap(Bitmap backgroundBitmap) {
        mBackgroundImageCenter = true;
        mBackgroundImage = backgroundBitmap;
        postInvalidate();
    }

    /**
     * sets padding
     * @param padding
     */
    public void setPadding(int padding) {
        mPadding = padding;
        postInvalidate();
    }

    public void setInnerCircleRatio(int innerCircleRatio) {
        mInnerCircleRatio = innerCircleRatio;
        postInvalidate();
    }

    public ArrayList<PieSlice> getSlices() {
        return mSlices;
    }

    public void setSlices(ArrayList<PieSlice> slices) {
        mSlices = slices;
        postInvalidate();
    }

    public PieSlice getSlice(int index) {
        return mSlices.get(index);
    }

    public void addSlice(PieSlice slice) {
        mSlices.add(slice);
        postInvalidate();
    }

    public void setOnSliceClickedListener(OnSliceClickedListener listener) {
        mListener = listener;
    }

    public void removeSlices() {
        mSlices.clear();
        postInvalidate();
    }

    @Override
    public int getDuration() {
        return mDuration;
    }

    @Override
    public void setDuration(int duration) {mDuration = duration;}

    @Override
    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    @Override
    public void setInterpolator(Interpolator interpolator) {mInterpolator = interpolator;}


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public boolean isAnimating() {
        if(mValueAnimator != null)
            return mValueAnimator.isRunning();
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public boolean cancelAnimating() {
        if (mValueAnimator != null)
            mValueAnimator.cancel();
        return false;
    }


    /**
     * Stops running animation and starts a new one, animating each slice from their current to goal value.
     * If removing a slice, consider animating to 0 then removing in onAnimationEnd listener.
     * Default inerpolator is linear; constant speed.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void animateToGoalValues() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1){
            Log.e("HoloGraphLibrary compatibility error", "Animation not supported on api level 12 and below. Returning without animating.");
            return;
        }
        if (mValueAnimator != null)
            mValueAnimator.cancel();

        for (PieSlice s : mSlices)
            s.setOldValue(s.getValue());
        ValueAnimator va = ValueAnimator.ofFloat(0,1);
        mValueAnimator = va;
        va.setDuration(getDuration());
        if (mInterpolator == null) mInterpolator = new LinearInterpolator();
        va.setInterpolator(mInterpolator);
        if (mAnimationListener != null) va.addListener(mAnimationListener);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = Math.max(animation.getAnimatedFraction(), 0.01f);//avoid blank frames; never multiply values by 0
               // Log.d("f", String.valueOf(f));
                for (PieSlice s : mSlices) {
                    float x = s.getGoalValue() - s.getOldValue();
                    s.setValue(s.getOldValue() + (x * f));
                }
                postInvalidate();
            }});
            va.start();

        }



    @Override
    public void setAnimationListener(Animator.AnimatorListener animationListener) { mAnimationListener = animationListener;}

    public interface OnSliceClickedListener {
        public abstract void onClick(int index);
    }
}
