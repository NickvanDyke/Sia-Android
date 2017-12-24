/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.common

import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class ResizeAnimation(private val view: View, private val startHeight: Int, private val targetHeight: Int) : Animation() {
    init {
        interpolator = FastOutSlowInInterpolator()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        println("view current height: ${view.height}")
        println("changing height to: ${(startHeight + (targetHeight - startHeight) * interpolatedTime).toInt()}")
        view.layoutParams.height = (startHeight + (targetHeight - startHeight) * interpolatedTime).toInt()
        if (!view.isInLayout)
            view.requestLayout()
    }

    override fun willChangeBounds() = true
}