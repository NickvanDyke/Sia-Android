/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vandyke.sia.util.rx.LiveEvent
import com.vandyke.sia.util.rx.MutableLiveEvent
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
        get() = stateInternal as LiveData<State>
    private val stateInternal = MutableLiveData<State>()

    val stateEvent
        get() = stateEventInternal as LiveEvent<State>
    private val stateEventInternal = MutableLiveEvent<State>()

    /** Should really only be called from SiadService */
    fun siadOutput(output: String) {
        if (output.contains("Finished loading")) {
            stateEventInternal.postValue(State.SIAD_LOADED)
            stateInternal.postValue(State.SIAD_LOADED)
        }
        mostRecentSiadOutputInternal.postValue(output)
        allSiadOutputInternal.onNext(output)
    }

    /** Should really only be called from SiadService */
    fun siadState(state: State) {
        stateEventInternal.postValue(state)
        stateInternal.postValue(state)
    }

    /** if you want specifics, check SiadService and see where each of these is set as the current state to see what causes each one */
    enum class State {
        /** SiadService has started */
        SERVICE_STARTED,
        /* the below four all occur when attempting to configure the siad process */
        COULDNT_COPY_BINARY,
        WORKING_DIRECTORY_DOESNT_EXIST,
        EXTERNAL_STORAGE_ERROR,
        COULDNT_START_PROCESS,
        /** the siad process is being configured (working directory, environment variables, etc.) and will then be started */
        STARTING_PROCESS,
        /* in both below states, the process has successfully started */
        SIAD_LOADING { override val processIsRunning = true },
        SIAD_LOADED { override val processIsRunning = true },
        /* the following are all states in which siad was previously running and has stopped */
        UNMET_CONDITIONS,
        CRASHED,
        MANUALLY_STOPPED,
        RESTARTING,
        SERVICE_STOPPED;

        open val processIsRunning = false
    }
}