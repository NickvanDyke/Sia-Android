package com.vandyke.sia.util

import android.view.View

fun View.goneUnless(value: Boolean) {
    this.visibility = if (value) View.VISIBLE else View.GONE
}

fun View.hiddenUnless(value: Boolean) {
    this.visibility = if (value) View.VISIBLE else View.INVISIBLE
}