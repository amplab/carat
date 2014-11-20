/*
 *     Created by Daniel Nadeau
 *     daniel.nadeau01@gmail.com
 *     danielnadeau.blogspot.com
 *
 *     Licensed to the Apache Software Foundation (ASF) under one
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
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class BarGraph extends View implements HoloGraphAnimate {

    private static final int VALUE_FONT_SIZE = 30;
    private static final int AXIS_LABEL_FONT_SIZE = 15;
    // How much space to leave between labels when shrunken. Increase for less space.
    private static final float LABEL_PADDING_MULTIPLIER = 1.6f;
    private static final int ORIENTATION_HORIZONTAL = 0;
    private static final int ORIENTATION_VERTICAL = 1;

    private final int mOrientation;
    private ArrayList<Bar> mBars = new ArrayList<Bar>();
    private Paint mPaint = new Paint();
    private Rect mBoundsRect = new Rect();
    private Rect mTextRect = new Rect();
    private boolean mShowAxis;
    private boolean mShowAxisLabel;
    private boolean mShowBarText;
    private boolean mShowPopup;
    private int mSelectedIndex = -1;
    private OnBarClickedListener mListener;
    private int mAxisColor;

    private int mDuration = 300;//in ms
    private Interpolator mInterpolator;
    private Animator.AnimatorListener mAnimationListener;
    private ValueAnimator mValueAnimator;
    private float mMaxValue;            //max value to use when animating
    private float mOldMaxValue;
    private float mGoalMaxValue;
    private long mLastTimeValueStringsUpdated;
    private long mValueStringUpdateInterval = 200;//ms; how often to update the value strings when animating
    private int mValueStringPrecision = 0;//how many decimals to put in the value string when animating; 0 for integers

    public BarGraph(Context context) {
        this(context, null);
    }

    public BarGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BarGraph);
        mOrientation = a.getInt(R.styleable.BarGraph_orientation, ORIENTATION_VERTICAL);
        mAxisColor = a.getColor(R.styleable.BarGraph_barAxisColor, Color.LTGRAY);
        mShowAxis = a.getBoolean(R.styleable.BarGraph_barShowAxis, true);
        mShowAxisLabel = a.getBoolean(R.styleable.BarGraph_barShowAxisLabel, true);
        mShowBarText = a.getBoolean(R.styleable.BarGraph_barShowText, true);
        mShowPopup = a.getBoolean(R.styleable.BarGraph_barShowPopup, true);
    }

    public void setShowAxis(boolean show) {
        mShowAxis = show;
    }

    public void setShowAxisLabel(boolean show) {
        mShowAxisLabel = show;
    }

    public void setShowBarText(boolean show) {
        mShowBarText = show;
    }

    public void setShowPopup(boolean show) {
        mShowPopup = show;
    }

    public void setBars(ArrayList<Bar> points) {
        mBars = points;
        postInvalidate();
    }

    public ArrayList<Bar> getBars() {
        return mBars;
    }

    public void setAxisColor(int axisColor) {
        mAxisColor = axisColor;
    }

    public void onDraw(Canvas canvas) {
        final Resources resources = getContext().getResources();

        canvas.drawColor(Color.TRANSPARENT);
        NinePatchDrawable popup = (NinePatchDrawable) resources.getDrawable(R.drawable.popup_black);

        float maxValue = 0;
        float padding = 7 * resources.getDisplayMetrics().density;
        float bottomPadding = 30 * resources.getDisplayMetrics().density;

        float usableHeight;
        if (mShowBarText) {
            mPaint.setTextSize(VALUE_FONT_SIZE * resources.getDisplayMetrics().scaledDensity);
            mPaint.getTextBounds("$", 0, 1, mTextRect);
            if (mShowPopup) {
                usableHeight = getHeight() - bottomPadding
                        - Math.abs(mTextRect.top - mTextRect.bottom)
                        - 24 * resources.getDisplayMetrics().density;
            } else {
                usableHeight = getHeight() - bottomPadding
                        - Math.abs(mTextRect.top - mTextRect.bottom)
                        - 18 * resources.getDisplayMetrics().density;
            }
        } else {
            usableHeight = getHeight() - bottomPadding;
        }

        // Draw x-axis line
        if (mShowAxis) {
            mPaint.setColor(mAxisColor);
            mPaint.setStrokeWidth(2 * resources.getDisplayMetrics().density);
            mPaint.setAntiAlias(true);
            canvas.drawLine(0,
                    getHeight() - bottomPadding + 10 * resources.getDisplayMetrics().density,
                    getWidth(),
                    getHeight() - bottomPadding + 10 * resources.getDisplayMetrics().density,
                    mPaint);
        }
        //Determine ideal bar size with number of bars at end not deleted

        int insertCount = 0;
        int deleteCount = 0;
        for (Bar bar :mBars)
        {
            if (bar.mAnimateSpecial ==ANIMATE_INSERT)
                insertCount++;

            if (bar.mAnimateSpecial ==ANIMATE_DELETE)
                deleteCount++;
        }
        int specialCount = insertCount + deleteCount;
        float barWidthHelper = (getWidth() - (padding * 2) * (mBars.size()-insertCount)) / (mBars.size()-insertCount);
        float insertHelper = (getWidth() - (padding * 2) * (mBars.size()-deleteCount)) / (mBars.size()-deleteCount);
        float specialWidthTotal = 0;

        int count = 0;
        float barWidths [] = new float[mBars.size()];
        for (final Bar bar : mBars) {   //calculate total widths of bars being inserted/deleted
            if (bar.mAnimateSpecial == ANIMATE_INSERT) {
                barWidths[count] =  (int) (getAnimatedFractionSafe() * insertHelper);
                specialWidthTotal +=  barWidths[count];

            }
            else if (bar.mAnimateSpecial == ANIMATE_DELETE){
                barWidths[count]  = (int) ( (1-getAnimatedFractionSafe()) * barWidthHelper);
                specialWidthTotal +=  barWidths[count];
            }
            count++;
        }
        specialWidthTotal += (deleteCount * (padding *2 *(1-getAnimationFraction())));
        specialWidthTotal += (insertCount * (padding *2 *getAnimationFraction()));
        int normalCount = mBars.size() - specialCount;
        //calculate the width of the exsisting normal bars
        float barWidth = (getWidth() - specialWidthTotal - (padding * 2 *normalCount)) / (normalCount);//calculate regular widths
        float defaultBarWidth = barWidth;
        for (int i = 0; i <mBars.size(); i++) if (mBars.get(i).mAnimateSpecial == ANIMATE_NORMAL) barWidths[i] = defaultBarWidth;

        //if animating, the max value is calculated for us
        if (isAnimating()){
            maxValue = mMaxValue;
        }
        else {
            for (final Bar bar : mBars) {
                if (bar.getValue() > maxValue) {
                    maxValue = bar.getValue();
                }
            }
            if (maxValue == 0) {
                maxValue = 1;
            }
        }

        count = 0;
        // Calculate the maximum text size for all the axis labels without regard to animation state so text doesn't jitter.
        // TODO there's probably a better way to do this.
        mPaint.setTextSize(AXIS_LABEL_FONT_SIZE
                * resources.getDisplayMetrics().scaledDensity);
        for (final Bar bar : mBars) {
            int left = (int) ((padding * 2) * count + padding + barWidth * count);
            int right = (int) ((padding * 2) * count + padding + barWidth * (count + 1));
            int width = (int)(defaultBarWidth + (padding *2));
            float textWidth = mPaint.measureText(bar.getName());
            // Decrease text size to fit and not overlap with other labels.
            while (right -left + (padding * LABEL_PADDING_MULTIPLIER) < textWidth) {
                mPaint.setTextSize(mPaint.getTextSize() - 1);
                float newTextWidth = mPaint.measureText(bar.getName());
                if (textWidth == newTextWidth) break;
                textWidth =newTextWidth;
            }
            count++;
        }
        // Save it to use later
        float labelTextSize = mPaint.getTextSize();

        count = 0;
        int oldright = (int) (padding *-1);
        int alpha = 255;//for bar color. Max values is bar.getColorAlpha
        int popupAlpha = 255;// for bar popup and text. Max value is 255;
        SparseArray<Float> valueTextSizes = new SparseArray<Float>();
        for (final Bar bar : mBars) {
            //Set alpha and width percentage if inserting or deleting
            if (isAnimating()){
                if (bar.mAnimateSpecial == ANIMATE_INSERT) {
                    alpha = ((int) (getAnimatedFractionSafe() * bar.getColorAlpha()));
                    popupAlpha = ((int) (getAnimatedFractionSafe() * 255));
                }
                else if (bar.mAnimateSpecial == ANIMATE_DELETE){
                    alpha = ((int) ((1 - getAnimatedFractionSafe()) * bar.getColorAlpha()));
                    popupAlpha = ((int) ((1- getAnimatedFractionSafe()) * 255));
            }
                else {
                    alpha = bar.getColorAlpha();
                    popupAlpha = 255;
                }
            }
            else {
                mPaint.setAlpha(bar.getColorAlpha());
                popupAlpha = 255;
            }
            barWidth = barWidths[count];
            // Set bar bounds
            int left = (int) (oldright + (padding * 2 *
                    (bar.mAnimateSpecial ==ANIMATE_DELETE ? 1-getAnimationFraction()://scale padding by animation time same as bar width
                    bar.mAnimateSpecial == ANIMATE_INSERT ? getAnimationFraction()
                    :1)));
            int top = (int) (getHeight() - bottomPadding
                    - (usableHeight * (bar.getValue() / maxValue)));
            int right = (int) (left + barWidth );
            int bottom = (int) (getHeight() - bottomPadding);
            oldright = right;
            mBoundsRect.set(left, top, right, bottom);

            // Draw bar
            if (count == mSelectedIndex && null != mListener) {
                mPaint.setColor(bar.getSelectedColor());
            } else {
                mPaint.setColor(bar.getColor());
            }
            if (isAnimating()) mPaint.setAlpha(alpha);
            canvas.drawRect(mBoundsRect, mPaint);

            // Create selection region
            Path p = bar.getPath();
            p.reset();
            p.addRect(mBoundsRect.left,
                    mBoundsRect.top,
                    mBoundsRect.right,
                    mBoundsRect.bottom,
                    Path.Direction.CW);
            bar.getRegion().set(mBoundsRect.left,
                    mBoundsRect.top,
                    mBoundsRect.right,
                    mBoundsRect.bottom);

            // Draw x-axis label text
            if (mShowAxisLabel) {
                mPaint.setColor(bar.getLabelColor());
                mPaint.setTextSize(labelTextSize);
                if (isAnimating()) mPaint.setAlpha(alpha);
                float textWidth = mPaint.measureText(bar.getName());
                int x = (int) (((mBoundsRect.left + mBoundsRect.right) / 2) - (textWidth / 2));
                int y = (int) (getHeight() - 3 * resources.getDisplayMetrics().scaledDensity);
                canvas.drawText(bar.getName(), x, y, mPaint);
            }

            // Draw value text
            if (mShowBarText) {
                mPaint.setTextSize(VALUE_FONT_SIZE
                        * resources.getDisplayMetrics().scaledDensity);
                mPaint.setColor(bar.getValueColor());
                if (isAnimating()) mPaint.setAlpha(popupAlpha);
                mPaint.getTextBounds(bar.getValueString(), 0, 1, mTextRect);

                int boundLeft = (int) (((mBoundsRect.left + mBoundsRect.right) / 2)
                        - (mPaint.measureText(bar.getValueString()) / 2)
                        - 10 * resources.getDisplayMetrics().density);
                int boundTop = (int) (mBoundsRect.top + (mTextRect.top - mTextRect.bottom)
                        - 18 * resources.getDisplayMetrics().density);
                int boundRight = (int) (((mBoundsRect.left + mBoundsRect.right) / 2)
                        + (mPaint.measureText(bar.getValueString()) / 2)
                        + 10 * resources.getDisplayMetrics().density);

                // Limit popup width to bar width
                if (boundLeft < mBoundsRect.left) {
                    boundLeft = mBoundsRect.left - ((int) padding / 2);
                }
                if (boundRight > mBoundsRect.right) {
                    boundRight = mBoundsRect.right + ((int) padding / 2);
                }

                if (mShowPopup) {
                    if (isAnimating()) popup.setAlpha(popupAlpha);
                    popup.setBounds(boundLeft, boundTop, boundRight, mBoundsRect.top);
                    popup.draw(canvas);
                }

                // Check cache to see if we've calculated value text size for bars that aren't being inserted/deleted.
                // Not 100% accurate with non monospace fonts but close enough
                // This is actually performance critical for 10+ bars.
                if (bar.mAnimateSpecial == ANIMATE_NORMAL) {
                    if (0 > valueTextSizes.indexOfKey(bar.getValueString().length())) {//cache miss
                        while (mPaint.measureText(bar.getValueString()) > boundRight - boundLeft) {
                            mPaint.setTextSize(mPaint.getTextSize() - (float) 1);
                        }
                        valueTextSizes.put(bar.getValueString().length(), mPaint.getTextSize());//cache save
                    } else {//cache hit
                        mPaint.setTextSize(valueTextSizes.get(bar.getValueString().length()));
                    }
                }   //for bars inserting/deleting, do the math everytime without a cache.
                    else
                        while (mPaint.measureText(bar.getValueString()) > boundRight - boundLeft) {
                            mPaint.setTextSize(mPaint.getTextSize() - (float) 1);
                }


                if (isAnimating()) mPaint.setAlpha(popupAlpha);
                canvas.drawText(bar.getValueString(),
                        (int) (((mBoundsRect.left + mBoundsRect.right) / 2)
                                - (mPaint.measureText(bar.getValueString())) / 2),
                        mBoundsRect.top - (mBoundsRect.top - boundTop) / 2f
                                + (float) Math.abs(mTextRect.top - mTextRect.bottom) / 2f * 0.7f,
                        mPaint
                );
            }
            count++;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        int count = 0;
        Region r = new Region();
        for (Bar bar : mBars) {
            r.setPath(bar.getPath(), bar.getRegion());
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
        // Reset selection
        if (MotionEvent.ACTION_UP == event.getAction()
                || MotionEvent.ACTION_CANCEL == event.getAction()) {
            mSelectedIndex = -1;
            postInvalidate();
        }
        return true;
    }

    public void setOnBarClickedListener(OnBarClickedListener listener) {
        mListener = listener;
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

    /**
     * Make sure your interpolator ends at exactly 1.0 (Bounce interpolator doesnt)
     * or else call makeValueString(int precision) on each bar in onAnimationEnd  so the value label will be the goal value.
     * @param interpolator
     */
    @Override
    public void setInterpolator(Interpolator interpolator) {mInterpolator = interpolator;}

    public int getmValueStringPrecision() {
        return mValueStringPrecision;
    }

    public void setValueStringPrecision(int valueStringPrecision) {mValueStringPrecision = valueStringPrecision;}

    public long getValueStringUpdateInterval() {
        return mValueStringUpdateInterval;
    }

    public void setValueStringUpdateInterval(long valueStringUpdateInterval) {mValueStringUpdateInterval = valueStringUpdateInterval;}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public boolean isAnimating() {
        if(mValueAnimator != null)
            return mValueAnimator.isRunning();
        return false;
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private float getAnimationFraction(){
        if (mValueAnimator != null && isAnimating())
            return mValueAnimator.getAnimatedFraction();
        else return 1f;
    }
    private float getAnimatedFractionSafe(){
        float f = getAnimationFraction();
        if (f >1) return 1;
        if (f < 0) return 0;
        else return f;
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public boolean cancelAnimating() {
        if (mValueAnimator != null)
            mValueAnimator.cancel();
        return false;
    }

    /**
     * Cancels then starts an animation to goal values, inserting or deleting bars.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void animateToGoalValues() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1){
            Log.e("HoloGraphLibrary compatibility error", "Animation not supported on api level 11 and below. Returning without animating.");
            return;
        }
        if (mValueAnimator != null)
            mValueAnimator.cancel();

        mOldMaxValue = 0;
        mGoalMaxValue = 0;
        for (Bar b : mBars) {
            b.setOldValue(b.getValue());
            mOldMaxValue = Math.max(mOldMaxValue, b.getValue());
            mGoalMaxValue = Math.max(mGoalMaxValue, b.getGoalValue());
        }
        mMaxValue = mOldMaxValue;
        ValueAnimator va = ValueAnimator.ofFloat(0,1);
        mValueAnimator = va;
        va.setDuration(getDuration());
        if (mInterpolator == null) mInterpolator = new LinearInterpolator();
        va.setInterpolator(mInterpolator);
        if (mAnimationListener != null) va.addListener(mAnimationListener);
        mLastTimeValueStringsUpdated = System.currentTimeMillis();
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = animation.getAnimatedFraction();
                // Log.d("f", String.valueOf(f));

                for (Bar b : mBars) {
                    float x = b.getGoalValue() - b.getOldValue();
                    b.setValue(b.getOldValue() + (x * f));
                    float xMax = mGoalMaxValue - mOldMaxValue;
                    mMaxValue = mOldMaxValue + (xMax * f);
                }
                long now = System.currentTimeMillis();
                //change value string after interval or when animation is at end point.
                if ((mLastTimeValueStringsUpdated + mValueStringUpdateInterval < now) || f==1f)
                {
                    for (Bar b : mBars)
                        b.makeValueString(mValueStringPrecision);
                    mLastTimeValueStringsUpdated = now;
                }
                postInvalidate();
            }});
        va.start();

    }



    @Override
    public void setAnimationListener(Animator.AnimatorListener animationListener) {
        mAnimationListener = animationListener;

    }

    public interface OnBarClickedListener {
        abstract void onClick(int index);
    }
}
