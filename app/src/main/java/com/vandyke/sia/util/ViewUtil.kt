package com.vandyke.sia.util

import android.view.View
import android.view.View.*

fun View.goneUnless(value: Boolean) {
    this.visibility = if (value) VISIBLE else GONE
}

fun View.hiddenUnless(value: Boolean) {
    this.visibility = if (value) VISIBLE else INVISIBLE
}

fun View.gone() {
    this.visibility = GONE
}

fun View.invisible() {
    this.visibility = INVISIBLE
}

fun View.visible() {
    this.visibility = VISIBLE
}

fun View.fadeToGone(duration: Long) {
    this.animate()
            .withLayer()
            .setDuration(duration)
            .alpha(0f)
            .withEndAction { this.visibility = GONE }
}

fun View.fadeToVisible(duration: Long) {
    this.animate()
            .withLayer()
            .setDuration(duration)
            .alpha(1f)
            .withStartAction { this.visibility = VISIBLE }
}