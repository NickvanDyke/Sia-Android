/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "dirs")
class Dir(
        @PrimaryKey
        override val path: String,
        override val size: Long
) : Node() {
    override val parent: String?
        get() = this.path.dirPathParent()
}

fun String.dirPathParent(): String? {
    val index = this.lastIndexOf('/')
    return if (index == -1) {
        if (this == "")
            null
        else
            ""
    } else {
        this.substring(0, index)
    }
}