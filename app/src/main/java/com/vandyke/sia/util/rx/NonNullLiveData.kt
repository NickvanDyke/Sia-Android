/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util.rx

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer

class NonNullLiveData<T>(initialValue: T) : LiveData<T>() {
    init {
        super.setValue(initialValue)
    }

    fun observe(owner: LifecycleOwner, onChanged: (T) -> Unit) {
        this.observe(owner, Observer {
            if (it == null)
                throw Exception()
            else
                onChanged(it)
        })
    }

    fun observeForevs(onChanged: (T) -> Unit) {
        this.observeForever {
            if (it == null)
                throw Exception()
            else
                onChanged(it)
        }
    }

    override fun getValue(): T {
        return super.getValue() ?: throw Exception()
    }

    public override fun setValue(value: T) {
        super.setValue(value)
    }

    public override fun postValue(value: T) {
        super.postValue(value)
    }
}