/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.arch.lifecycle.MutableLiveData
import com.vandyke.sia.util.rx.NonNullLiveData
import com.vandyke.sia.util.rx.SingleLiveEvent
import io.reactivex.subjects.ReplaySubject
import javax.inject.Inject
import javax.inject.Singleton

/** singleton object that holds siad output, state, etc */
@Singleton
class SiadStatus
@Inject constructor() {
    /** Don't modify directly. Call siadOutput() */
    val mostRecentSiadOutput = MutableLiveData<String>()
    /** Don't modify directly. Call siadOutput() */
    val allSiadOutput = ReplaySubject.create<String>()!!

    /** Don't modify directly. Call siadState() */
    val state = NonNullLiveData(State.STOPPED)
    /** Don't modify directly. Call siadState() */
    val stateEvent = SingleLiveEvent<State>()

    fun siadOutput(output: String) {
        if (output.contains("Finished loading")) {
            stateEvent.postValue(State.SIAD_LOADED)
            state.postValue(State.SIAD_LOADED)
        }
        mostRecentSiadOutput.postValue(output)
        allSiadOutput.onNext(output)
    }

    fun siadState(state: State) {
        stateEvent.value = state
        this.state.value = state
    }

    enum class State {
        STOPPING,
        STOPPED,
        PROCESS_STARTING,
        SIAD_LOADING,
        SIAD_LOADED
    }
}