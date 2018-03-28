package com.vandyke.sia.util.rx

class MutableNonNullLiveData<T>(initialValue: T) : NonNullLiveData<T>(initialValue) {
    public override fun setValue(value: T) {
        super.setValue(value)
    }

    public override fun postValue(value: T) {
        super.postValue(value)
    }
}