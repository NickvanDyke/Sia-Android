/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json

sealed class Node {
    abstract val path: String
    abstract val size: Long
    abstract val parent: String?

    val name: String by lazy { this.path.name() }
}

/** We use this because generally when using the node's path as a parent path, or adding to it,
  * we add a slash to the end of the path. But if this node resides in the root directory,
  * then it's parent is the empty string, which we don't want to add a slash to. */
fun String.withTrailingSlashIfNotEmpty() = if (this.isNotEmpty()) "$this/" else this

fun String.name(): String = this.substring(this.lastIndexOf('/') + 1)

fun List<Node>.sumSize(): Long {
    var size = 0L
    this.forEach { node ->
        size += node.size
    }
    return size
}

@Entity(tableName = "files")
data class SiaFile(
        @PrimaryKey
        @Json(name = "siapath")
        override val path: String,
        @Json(name = "localpath")
        val localpath: String,
        @Json(name = "filesize")
        override val size: Long, // bytes
        @Json(name = "available")
        val available: Boolean,
        @Json(name = "renewing")
        val renewing: Boolean,
        @Json(name = "redundancy")
        val redundancy: Double,
        @Json(name = "uploadedbytes")
        val uploadedBytes: Long,
        @Json(name = "uploadprogress")
        val uploadProgress: Int,
        @Json(name = "expiration")
        val expiration: Long) : Node() {

    override val parent: String
        get() = this.path.filePathParent()
}

fun String.filePathParent(): String {
    val index = this.lastIndexOf('/')
    return if (index == -1) {
        ""
    } else {
        this.substring(0, index)
    }
}

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