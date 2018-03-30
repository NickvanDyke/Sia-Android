/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

data class DownloadData(
        val destination: String,
        val destinationtype: String,
        val length: Long,
        val offset: Long,
        val siapath: String,
        val completed: Boolean,
        val endtime: String,
        val error: String,
        val received: Long,
        val starttime: String,
        val totaldatatransferred: Long) {

    val status = when {
        completed && error.isEmpty() -> Status.COMPLETED_SUCCESSFULLY
        !completed && error.isEmpty() -> Status.IN_PROGRESS
        completed && error.isNotEmpty() -> Status.ERROR_OCCURRED
        else -> throw IllegalStateException()
    }
    /** current download progress, out of 100 */
    val progress = ((received.toFloat() / length.toFloat()) * 100).toInt()

    // careful that this doesn't cause any funky behavior
    override fun equals(other: Any?): Boolean {
        if (other !is DownloadData)
            return false
        return this.siapath == other.siapath && this.destination == other.destination
    }

    enum class Status {
        COMPLETED_SUCCESSFULLY,
        IN_PROGRESS,
        ERROR_OCCURRED
    }
}

