package vandyke.siamobile.ui.custom

import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class ResizeAnimation(var view: View, var startHeight: Int, val targetHeight: Int) : Animation() {
    init {
        interpolator = FastOutSlowInInterpolator()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        view.layoutParams.height = (startHeight + (targetHeight - startHeight) * interpolatedTime).toInt()
        view.requestLayout()
    }

    override fun willChangeBounds() = true
}