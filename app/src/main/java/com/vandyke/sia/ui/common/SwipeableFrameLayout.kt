/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Animation
import android.widget.FrameLayout

class SwipeableFrameLayout : FrameLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    var onSwipeUp: (() -> Unit)? = null

    val swipeListener = GestureDetector(context, object : OnSwipeListener() {
        override fun onSwipe(direction: Direction): Boolean {
            if (direction == Direction.up) {
                onSwipeUp?.invoke()
                return true
            }
            return false
        }
    })

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        swipeListener.onTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    fun expandVertically(endHeight: Int) {
        val animation = ResizeAnimation(this, endHeight)
        animation.duration = 300
        startAnimation(animation)
    }

    fun collapseVertically(onCollapseFinished: (() -> Unit)? = null) {
        val animation = ResizeAnimation(this, 0)
        animation.duration = 300
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                onCollapseFinished?.invoke()
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
        startAnimation(animation)
    }
}