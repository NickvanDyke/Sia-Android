/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.models.renter

abstract class Node {
    abstract val path: String
    abstract val size: Long
    abstract val parent: String?

    val name: String
        get() = this.path.name()
}

fun String.withTrailingSlashIfNotEmpty() = if (this.isNotEmpty()) "$this/" else this

fun String.name(): String = this.substring(this.lastIndexOf('/') + 1)

fun List<Node>.sumSize(): Long {
    var size = 0L
    this.forEach { node ->
        size += node.size
    }
    return size
}