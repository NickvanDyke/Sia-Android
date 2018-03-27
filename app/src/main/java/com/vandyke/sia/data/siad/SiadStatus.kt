/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.arch.lifecycle.MutableLiveData
import com.vandyke.sia.util.rx.NonNullLiveData
import io.reactivex.subjects.ReplaySubject
import javax.inject.Inject
import javax.inject.Singleton

/** singleton object that holds siad status, output, etc */
@Singleton
class SiadStatus
@Inject constructor() {
    /** Don't modify directly. Call siadOutput() */
    val mostRecentSiadOutput = MutableLiveData<String>()

    /** Don't modify directly. Call siadOutput() */
    val allSiadOutput = ReplaySubject.create<String>()!!

    val state = NonNullLiveData(State.STOPPED)

    fun siadOutput(output: String) {
        if (output.contains("Finished loading"))
            state.value = State.SIAD_LOADED
        mostRecentSiadOutput.postValue(output)
        allSiadOutput.onNext(output)
    }

    enum class State {
        STOPPING,
        STOPPED,
        PROCESS_STARTING,
        SIAD_LOADING,
        SIAD_LOADED
    }
}