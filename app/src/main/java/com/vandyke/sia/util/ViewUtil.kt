package com.vandyke.sia.util

import android.view.View

fun View.goneUnless(value: Boolean) {
    this.visibility = if (value) View.VISIBLE else View.GONE
}

fun View.hiddenUnless(value: Boolean) {
    this.visibility = if (value) View.VISIBLE else View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.fadeToGone(duration: Long) {
    this.animate()
            .setDuration(duration)
            .alpha(0f)
            .withEndAction { this.visibility = View.GONE }
}

fun View.fadeToVisible(duration: Long) {
    this.animate()
            .setDuration(duration)
            .alpha(1f)
            .withEndAction { this.visibility = View.VISIBLE }
}