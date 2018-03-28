package com.vandyke.sia.util.rx

import android.support.annotation.MainThread

class MutableSingleLiveEvent<T> : SingleLiveEvent<T>() {
    @MainThread
    public override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    public override fun postValue(value: T) {
        super.postValue(value)
    }
}