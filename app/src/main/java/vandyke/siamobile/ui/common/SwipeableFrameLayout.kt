package vandyke.siamobile.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import vandyke.siamobile.util.GenUtil

class SwipeableFrameLayout : FrameLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    val swipeListener = GestureDetector(context, object : OnSwipeListener() {
        override fun onSwipe(direction: Direction): Boolean {
            if (direction == Direction.up) {
                collapse()
                return true
            }
            return false
        }
    })

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        swipeListener.onTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    fun expand(endHeight: Int) {
        // TODO: expanding will sometimes lag depending on what the fragment that was loaded in is doing?
        // Seems to happen when calls to the viewModel are made in the child fragment
//        println("expanding from $height to $endHeight")
        val animation = ResizeAnimation(this, height, endHeight)
        animation.duration = 300
        startAnimation(animation)
    }

    fun collapse() {
//        println("collapsing from $height")
        val animation = ResizeAnimation(this, height, 0)
        animation.duration = 300
        startAnimation(animation)
        GenUtil.hideSoftKeyboard(rootView)
    }
}