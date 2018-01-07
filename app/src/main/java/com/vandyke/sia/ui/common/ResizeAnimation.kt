/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class ResizeAnimation(private val view: View, private val targetHeight: Int) : Animation() {
    private val startHeight = view.height

    init {
        println("start height: $startHeight  target height: $targetHeight")
        interpolator = FastOutSlowInInterpolator()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        view.layoutParams.height = (startHeight + (targetHeight - startHeight) * interpolatedTime).toInt()
        if (!view.isInLayout)
            view.requestLayout()
    }

    override fun willChangeBounds() = true
}