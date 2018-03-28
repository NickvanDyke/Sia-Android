/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.vandyke.sia.util.rx.LiveEvent
import com.vandyke.sia.util.rx.MutableLiveEvent
import com.vandyke.sia.util.rx.MutableNonNullLiveData
import com.vandyke.sia.util.rx.NonNullLiveData
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import javax.inject.Inject
import javax.inject.Singleton

/** singleton object that holds siad output, state, etc */
@Singleton
class SiadStatus
@Inject constructor() {
    /* reason for all these getters and casts: so that values can only be modified within this class
     * by calling siadOutput and siadState externally */
    val mostRecentSiadOutput
        get() = mostRecentSiadOutputInternal as LiveData<String>
    private val mostRecentSiadOutputInternal = MutableLiveData<String>()

    val allSiadOutput
        get() = allSiadOutputInternal as Observable<String>
    private val allSiadOutputInternal = ReplaySubject.create<String>()!!

    val state
        get() = stateInternal as NonNullLiveData<State>
    private val stateInternal = MutableNonNullLiveData(State.STOPPED)

    val stateEvent
        get() = stateEventInternal as LiveEvent<State>
    private val stateEventInternal = MutableLiveEvent<State>()

    fun siadOutput(output: String) {
        if (output.contains("Finished loading")) {
            stateEventInternal.postValue(State.SIAD_LOADED)
            stateInternal.postValue(State.SIAD_LOADED)
        }
        mostRecentSiadOutputInternal.postValue(output)
        allSiadOutputInternal.onNext(output)
    }

    fun siadState(state: State) {
        stateEventInternal.value = state
        this.stateInternal.value = state
    }

    enum class State {
        STOPPING,
        STOPPED,
        PROCESS_STARTING,
        SIAD_LOADING,
        SIAD_LOADED
    }
}