/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util.rx

import android.arch.lifecycle.LiveData

open class NonNullLiveData<T>(initialValue: T) : LiveData<T>() {
    init {
        super.setValue(initialValue)
    }

    override fun getValue(): T {
        return super.getValue() ?: throw IllegalStateException()
    }


}