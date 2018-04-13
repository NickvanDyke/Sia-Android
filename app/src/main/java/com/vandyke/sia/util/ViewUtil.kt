package com.vandyke.sia.util

import android.support.v7.preference.PreferenceManager
import android.view.View
import android.view.View.*
import com.vandyke.sia.R
import it.sephiroth.android.library.tooltip.Tooltip

fun View.goneUnless(value: Boolean) {
    this.visibility = if (value) VISIBLE else GONE
}

fun View.invisibleUnless(value: Boolean) {
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

fun View.oneTimeTooltip(text: CharSequence, gravity: Tooltip.Gravity): Tooltip.TooltipView? {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)
    return if (!prefs.getBoolean(this.id.toString(), false))
        Tooltip.make(this.context, Tooltip.Builder()
                .text(text)
                .closePolicy(Tooltip.ClosePolicy.TOUCH_ANYWHERE_CONSUME, 0)
                .withStyleId(R.style.TooltipLayout)
                .withOverlay(false)
                .withCallback(object : Tooltip.Callback {
                    override fun onTooltipClose(p0: Tooltip.TooltipView?, p1: Boolean, p2: Boolean) {
                        prefs.edit().putBoolean(this@oneTimeTooltip.id.toString(), true).commit()
                    }

                    override fun onTooltipFailed(p0: Tooltip.TooltipView?) {
                    }

                    override fun onTooltipHidden(p0: Tooltip.TooltipView?) {
                    }

                    override fun onTooltipShown(p0: Tooltip.TooltipView?) {
                    }
                })
                .anchor(this, gravity))
    else
        null
}