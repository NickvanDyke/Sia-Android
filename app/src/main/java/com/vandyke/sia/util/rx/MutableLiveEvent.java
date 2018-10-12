package com.vandyke.sia.util.rx;

import android.util.Pair;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

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
