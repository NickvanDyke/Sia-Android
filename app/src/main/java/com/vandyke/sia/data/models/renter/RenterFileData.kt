/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.repository.ROOT_DIR_NAME
import java.math.BigDecimal

@Entity(tableName = "files")
data class RenterFileData
@JsonCreator constructor(
        @JsonProperty(value = "siapath")
        val siapath: String,
        @JsonProperty(value = "localpath")
        val localpath: String,
        @JsonProperty(value = "filesize")
        val filesize: BigDecimal, // bytes
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
        val expiration: Long) : Node("$ROOT_DIR_NAME/$siapath", filesize) {
    @Ignore
    val siapathParent = run {
        val index = this.siapath.lastIndexOf('/')
        if (index == -1)
            null
        else
            this.siapath.substring(0, index)
    }
}