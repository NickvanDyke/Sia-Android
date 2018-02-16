package com.vandyke.sia.util.rx

import android.view.View

fun View.visibleIf(value: Boolean) {
    this.visibility = if (value) View.VISIBLE else View.GONE
}