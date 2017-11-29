package vandyke.siamobile.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
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
        return false
    }

    fun collapse() {
        visibility = View.GONE
        GenUtil.hideSoftKeyboard(rootView)
    }
}