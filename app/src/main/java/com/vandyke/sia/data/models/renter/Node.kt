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

    val name: String
        get() = this.path.name()
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
        @field:Json(name = "siapath")
        override val path: String,
        val localpath: String, /* empty if local file is no longer present */
        @field:Json(name = "filesize")
        override val size: Long, // bytes
        val available: Boolean,
        val renewing: Boolean,
        val redundancy: Double,
        val uploadedbytes: Long,
        val uploadprogress: Float,
        val expiration: Long) : Node() {

    override val parent: String
        get() = this.path.filePathParent()

    companion object {
        /** Convenience method that returns a SiaFile with the given path and default values for other fields  */
        fun withPath(path: String) = SiaFile(path, "", 0, false, false, 0.0, 0, 0f, 0)

        fun withDefaults(path: String = "", localpath: String = "", size: Long = 0L, available: Boolean = false, renewing: Boolean = false,
                         redundancy: Double = 0.0, uploadedbytes: Long = 0L, uploadprogress: Float = 0f, expiration: Long = 0L): SiaFile {
            return SiaFile(path, localpath, size, available, renewing, redundancy, uploadedbytes, uploadprogress, expiration)
        }
    }
}

fun String.filePathParent(): String {
    val index = this.lastIndexOf('/')
    return if (index == -1) {
        ""
    } else {
        this.substring(0, index)
    }
}

fun String.fileExtension(): String {
    val dotIndex = this.lastIndexOf('.')
    return when (dotIndex) {
        -1 -> "" // do I want null here instead?
        this.length - 1 -> ""
        else -> this.substring(dotIndex + 1) /* don't include the dot in the returned extension */
    }
}

@Entity(tableName = "dirs")
data class Dir(
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