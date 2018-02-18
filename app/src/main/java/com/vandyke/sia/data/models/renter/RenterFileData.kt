/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.vandyke.sia.data.local.models.renter.Node
import java.math.BigDecimal

@Entity(tableName = "files")
data class RenterFileData
@JsonCreator constructor(
        @PrimaryKey
        @JsonProperty(value = "siapath")
        override val path: String,
        @JsonProperty(value = "localpath")
        val localpath: String,
        @JsonProperty(value = "filesize")
        override val size: BigDecimal, // bytes
        @JsonProperty(value = "available")
        val available: Boolean,
        @JsonProperty(value = "renewing")
        val renewing: Boolean,
        @JsonProperty(value = "redundancy")
        val redundancy: Double,
        @JsonProperty(value = "uploadedbytes")
        val uploadedBytes: Long,
        @JsonProperty(value = "uploadprogress")
        val uploadProgress: Int,
        @JsonProperty(value = "expiration")
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