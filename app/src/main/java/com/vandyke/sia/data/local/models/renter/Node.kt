/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.models.renter

import java.math.BigDecimal

abstract class Node {
    abstract val path: String
    abstract val size: BigDecimal

    val name: String
        get() = this.path.name()

    val parent: String?
        get() = this.path.parent()
}

fun String.withTrailingSlashIfNotEmpty() = if (this.isNotEmpty()) "$this/" else this

fun String.parent(): String? {
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

fun String.name(): String = this.substring(this.lastIndexOf('/') + 1)