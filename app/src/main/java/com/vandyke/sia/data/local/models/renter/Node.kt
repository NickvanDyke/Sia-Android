/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.models.renter

import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.vandyke.sia.util.slashStart

open class Node(path: String) {

    /* ensure that the path always starts with a / */
    @PrimaryKey
    var path = path.slashStart()

    @Ignore
    val name = this.path.substring(this.path.lastIndexOf('/') + 1)

    @Ignore
    val parent = this.path.substring(0, this.path.lastIndexOf('/'))
}