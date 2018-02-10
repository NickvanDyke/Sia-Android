/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.models.renter

import java.math.BigDecimal

abstract class Node {
    abstract val path: String
    abstract val size: BigDecimal

    val name: String
        get() = this.path.substring(this.path.lastIndexOf('/') + 1)

    val parent: String?
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

fun String.withTrailingSlashIfNotEmpty() = if (this.isNotEmpty()) "$this/" else this