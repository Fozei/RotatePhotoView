/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package uk.co.senab.photoview.gestures;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import uk.co.senab.photoview.log.LogManager;

public class CupcakeGestureDetector implements GestureDetector {

    protected OnGestureListener mListener;
    private static final String LOG_TAG = "CupcakeGestureDetector";
    float mLastTouchX;
    float mLastTouchY;
    final float mTouchSlop;
    final float mMinimumVelocity;
    private int mode = 0, single = 1, multi = 2;// 设置点击的模式，是单点触摸还是多点
    private PointF midPoint; // 两点的中心位置
    private float startRotation;

    @Override
    public void setOnGestureListener(OnGestureListener listener) {
        this.mListener = listener;
    }

    public CupcakeGestureDetector(Context context) {
        final ViewConfiguration configuration = ViewConfiguration
                .get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    private VelocityTracker mVelocityTracker;
    private boolean mIsDragging;

    float getActiveX(MotionEvent ev) {
        return ev.getX();
    }

    float getActiveY(MotionEvent ev) {
        return ev.getY();
    }

    @Override
    public boolean isScaling() {
        return false;
    }

    @Override
    public boolean isDragging() {
        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mode = single;
                mVelocityTracker = VelocityTracker.obtain();
                if (null != mVelocityTracker) {
                    mVelocityTracker.addMovement(ev);
                } else {
                    LogManager.getLogger().i(LOG_TAG, "Velocity tracker is null");
                }

                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                mIsDragging = false;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = multi;
                startRotation = getRotation(ev);
                midPoint = getMidPoint(ev);
                break;

            case MotionEvent.ACTION_MOVE: {
                if (mode == single) {
                    final float x = getActiveX(ev);
                    final float y = getActiveY(ev);
                    final float dx = x - mLastTouchX, dy = y - mLastTouchY;

                    if (!mIsDragging) {
                        // Use Pythagoras to see if drag length is larger than
                        // touch slop
                        mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                    }

                    if (mIsDragging) {
                        mListener.onDrag(dx, dy);
                        mLastTouchX = x;
                        mLastTouchY = y;

                        if (null != mVelocityTracker) {
                            mVelocityTracker.addMovement(ev);
                        }
                    }
                } else if (mode == multi) {
                    final float x = getActiveX(ev);
                    final float y = getActiveY(ev);
                    final float dx = x - mLastTouchX, dy = y - mLastTouchY;

                    if (!mIsDragging) {
                        // Use Pythagoras to see if drag length is larger than
                        // touch slop
                        mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                    }

                    mListener.onDrag(dx, dy);
                    float endRotate = getRotation(ev);
                    float rotate = endRotate - startRotation;
                    startRotation = endRotate;
                    Log.i(LOG_TAG, "CupcakeGestureDetector.onTouchEvent:" + endRotate);
                    mListener.onRotate(rotate, midPoint.x, midPoint.y);
                    mLastTouchX = x;
                    mLastTouchY = y;

                    if (null != mVelocityTracker) {
                        mVelocityTracker.addMovement(ev);
                    }
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mIsDragging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(ev);
                        mLastTouchY = getActiveY(ev);

                        // Compute velocity within the last 1000ms
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker
                                .getYVelocity();

                        // If the velocity is greater than minVelocity, call
                        // listener
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mListener.onFling(mLastTouchX, mLastTouchY, -vX,
                                    -vY);
                        }
                    }
                }

                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }

        return true;
    }

    /**
     *
     * @param event
     * @return 取旋转角度
     */
    private float getRotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    /**
     *
     * @param event
     * @return 两个手指的中心点
     */
    private PointF getMidPoint(MotionEvent event) {
        float midx = (event.getX(0) + event.getX(1)) / 2;
        float midy = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(midx, midy);
    }
}
