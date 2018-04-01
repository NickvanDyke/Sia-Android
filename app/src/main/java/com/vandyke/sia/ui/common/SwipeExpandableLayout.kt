package com.vandyke.sia.ui.common

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import net.cachapa.expandablelayout.ExpandableLayout

class SwipeExpandableLayout : ExpandableLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    private val detector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        // leaving this in for one commit so that if I try to figure it out later, I can refer to here initially
//        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
//            dragging = true
//            val fullHeight = measuredHeight / expansion // this is wrong
//            val changeExpansionBy = -(distanceY / fullHeight)
//            val clampedPercent = changeExpansionBy.coerceIn(-expansion, 1 - expansion)
//            println("measuredHeight: $measuredHeight; expansion: $expansion; fullHeight: $fullHeight; raw percent: $changeExpansionBy; clampedPercent: $clampedPercent")
//            expansion += (clampedPercent * expansion)
//            return true
//        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (velocityY < -1000)
                collapse()
            return true
        }
    })

    /* this is called when one of our children is touched (heh). We return false because we want it
     * to pass down to the child, instead of receiving succeeding touch events in our onTouchEvent */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)
        return false
    }

    /* this is called when our view is touched (a.k.a. an area that no children are covering is touched).
     * We return true because we want to continue receiving succeeding touch events here */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)
        return true
    }
}