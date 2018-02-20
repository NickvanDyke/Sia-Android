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
    val allSiadOutput = ReplaySubject.create<String>()

    val isSiadLoaded = NonNullLiveData(false)

    fun siadOutput(output: String) {
        mostRecentSiadOutput.value = output
        allSiadOutput.onNext(output)
    }
}