/*
 * Copyright (c) 2025 Sankalp Tharu
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information.
 */
 
package com.github.essencelauncher.utils.listener

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class GestureManager(
    private val context: Context,
    private val listener: GestureListener
) : GestureDetector.SimpleOnGestureListener() {

    private val gestureDetector = GestureDetector(context, this)
    private var downTime: Long = 0
    private var isScrolling = false
    private var flingDetected = false
    private var initialEvent: MotionEvent? = null

    private val handler = Handler(Looper.getMainLooper())
    private var scrollFinishedRunnable: Runnable? = null

    interface GestureListener {
        fun onSwipeLeft()
        fun onSwipeRight()
        fun onSwipeUp()
        fun onSwipeDown()
        fun onLongPress()
        fun onDoubleTap()
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downTime = System.currentTimeMillis()
                isScrolling = false
                flingDetected = false
                scrollFinishedRunnable?.let { handler.removeCallbacks(it) }
                initialEvent?.recycle()
                initialEvent = MotionEvent.obtain(event)
            }
            MotionEvent.ACTION_UP -> {
                if (isScrolling && initialEvent != null) {
                    handleScrollFinished(initialEvent!!, event)
                    isScrolling = false
                    initialEvent?.recycle()
                    initialEvent = null
                }
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    override fun onDown(event: MotionEvent): Boolean {
        downTime = System.currentTimeMillis()
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        listener.onDoubleTap()
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        listener.onLongPress()
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        isScrolling = true
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 == null) return false
        scrollFinishedRunnable?.let { handler.removeCallbacks(it) }
        val duration = System.currentTimeMillis() - downTime
        val isHorizontal = abs(e2.x - e1.x) > abs(e2.y - e1.y)
        val velocity = if (isHorizontal) abs(velocityX) else abs(velocityY)
        val handled = detectSwipeGesture(e1, e2, duration, velocity)
        flingDetected = handled
        return handled
    }

    private fun handleScrollFinished(e1: MotionEvent, e2: MotionEvent) {
        scrollFinishedRunnable?.let { handler.removeCallbacks(it) }
        val duration = System.currentTimeMillis() - downTime
        scrollFinishedRunnable = Runnable {
            if (!flingDetected) {
                detectSwipeGesture(e1, e2, duration)
            }
            flingDetected = false
        }
        handler.postDelayed(scrollFinishedRunnable!!, 50)
    }

    private fun detectSwipeGesture(
        startEvent: MotionEvent,
        endEvent: MotionEvent,
        duration: Long,
        velocity: Float? = null
    ): Boolean {
        val diffX = endEvent.x - startEvent.x
        val diffY = endEvent.y - startEvent.y
        val isHorizontalSwipe = abs(diffX) > abs(diffY)
        
        // Threshold values for swipe detection
        val distanceThreshold = 100f // Minimum distance for swipe
        val velocityThreshold = 1000f // Minimum velocity for fling

        val distance = if (isHorizontalSwipe) abs(diffX) else abs(diffY)
        val isSwipe = distance >= distanceThreshold

        // For fling gestures, check velocity threshold
        if (velocity != null && abs(velocity) < velocityThreshold) {
            return false
        }

        if (isSwipe) {
            when {
                isHorizontalSwipe && diffX > 0 -> {
                    listener.onSwipeRight()
                    return true
                }
                isHorizontalSwipe && diffX < 0 -> {
                    listener.onSwipeLeft()
                    return true
                }
                !isHorizontalSwipe && diffY > 0 -> {
                    listener.onSwipeDown()
                    return true
                }
                !isHorizontalSwipe && diffY < 0 -> {
                    listener.onSwipeUp()
                    return true
                }
            }
        }
        return false
    }
}
