package com.vandyke.sia.util.rx;

import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.concurrent.atomic.AtomicBoolean;

public class MutableLiveEvent<T> extends LiveEvent<T> {
    @Override
    @MainThread
    public void setValue(@Nullable T t) {
        synchronized (mPendingObservers) {
            for(Pair<Observer<T>, AtomicBoolean> pending : mPendingObservers.values()) {
                pending.second.set(true);
            }
        }
        super.setValue(t);
    }

    @Override
    public void postValue(@Nullable T t) {
        super.postValue(t);
    }
}
