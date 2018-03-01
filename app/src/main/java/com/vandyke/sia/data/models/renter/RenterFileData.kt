/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json
import com.vandyke.sia.data.local.models.renter.Node
import java.math.BigDecimal

@Entity(tableName = "files")
data class RenterFileData(
        @PrimaryKey
        @Json(name = "siapath")
        override val path: String,
        @Json(name = "localpath")
        val localpath: String,
        @Json(name = "filesize")
        override val size: BigDecimal, // bytes
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