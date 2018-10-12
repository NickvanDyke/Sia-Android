/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.settings

import android.content.Context
import android.util.AttributeSet
import com.takisoft.preferencex.SimpleMenuPreference

class IntSimpleMenuPreference : SimpleMenuPreference {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun getPersistedString(defaultReturnValue: String?): String {
        return Integer.toString(getPersistedInt(-1))
    }

    override fun persistString(value: String): Boolean {
        return persistInt(Integer.valueOf(value))
    }
}