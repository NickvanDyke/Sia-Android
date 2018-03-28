package com.vandyke.sia.util.rx;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A lifecycle-aware observable that sends only new updates after subscription, used for events like
 * navigation and Snackbar messages.
 *
 *
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 */
public class LiveEvent<T> extends LiveData<T> {

    final ConcurrentHashMap<Observer<T>, Pair<Observer<T>, AtomicBoolean>> mPendingObservers = new ConcurrentHashMap<>();

    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {

        Observer<T> interceptor = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                Observer<T> observerToTrigger = null;
                synchronized (mPendingObservers) {
                    if(mPendingObservers.containsKey(this) && mPendingObservers.get(this).second.compareAndSet(true, false)) {
                        observerToTrigger = mPendingObservers.get(this).first;
                    }
                }
                if(observerToTrigger != null) {
                    observerToTrigger.onChanged(t);
                }
            }
        };

        synchronized (mPendingObservers) {
            mPendingObservers.put(interceptor, Pair.create(observer, new AtomicBoolean(false)));
        }

        // Observe the internal MutableLiveData
        super.observe(owner, interceptor);
    }

    @Override
    public void removeObserver(@NonNull Observer<T> observer) {
        super.removeObserver(observer);
        synchronized (mPendingObservers) {
            for(Map.Entry<Observer<T>, Pair<Observer<T>, AtomicBoolean>> entry : mPendingObservers.entrySet()) {
                if(observer == entry.getKey() || observer == entry.getValue().first) {
                    mPendingObservers.remove(entry.getKey());
                    break;
                }
            }
        }
    }

    @MainThread
    public void setValue(@Nullable T t) {
        synchronized (mPendingObservers) {
            for(Pair<Observer<T>, AtomicBoolean> pending : mPendingObservers.values()) {
                pending.second.set(true);
            }
        }
        super.setValue(t);
    }
}
