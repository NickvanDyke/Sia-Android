/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.models.renter

import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

open class Node(@PrimaryKey var path: String, var modified: Long = System.currentTimeMillis()) {

    var name = this.path.substring(this.path.lastIndexOf('/') + 1)

    @Ignore
    val parent = run {
        val index = this.path.lastIndexOf('/')
        if (index == -1)
            null
        else
            this.path.substring(0, index)
    }
}