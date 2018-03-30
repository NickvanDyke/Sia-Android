package com.vandyke.sia.util

import android.animation.Animator
import android.view.View

fun View.goneUnless(value: Boolean) {
    this.visibility = if (value) View.VISIBLE else View.GONE
}

fun View.hiddenUnless(value: Boolean) {
    this.visibility = if (value) View.VISIBLE else View.INVISIBLE
}

fun View.fadeToGone(duration: Long) {
    this.animate()
            .setDuration(duration)
            .alpha(0f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    this@fadeToGone.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }
            })
            .start()
}

fun View.fadeToVisible(duration: Long) {
    this.animate()
            .setDuration(duration)
            .alpha(1f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    this@fadeToVisible.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }
            })
            .start()
}