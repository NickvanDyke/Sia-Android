/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "dirs")
class Dir(
        @PrimaryKey
        override val path: String,
        override val size: BigDecimal
) : Node() {
    override val parent: String?
        get() {
            val index = this.path.lastIndexOf('/')
            return if (index == -1) {
                if (this.path == "")
                    null
                else
                    ""
            } else {
                this.path.substring(0, index)
            }
        }
}