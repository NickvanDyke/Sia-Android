/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.data.renter

import java.math.BigDecimal


data class SiaFile(val siapath: String = "",
                   val filesize: BigDecimal = BigDecimal.ZERO, // bytes
                   val available: Boolean = false,
                   val renewing: Boolean = false,
                   val redundancy: Int = 0,
                   val uploadprogress: Int = 0,
                   val expiration: Long = 0) : SiaNode() {
    override lateinit var parent: SiaDir
    override val name by lazy { siapath.substring(siapath.lastIndexOf("/") + 1) }
    override val size: BigDecimal
        get() = filesize

    override fun equals(other: Any?): Boolean {
        if (other is SiaFile) {
            return this.siapath == other.siapath
        } else {
            return false
        }
    }
}