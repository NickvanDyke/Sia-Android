/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.model

import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.backend.data.renter.SiaFile
import vandyke.siamobile.backend.networking.SiaCallback
import java.math.BigDecimal

class RenterModelTest : IRenterModel {
    override fun getRootDir(callback: SiaCallback<SiaDir>) {
        val rootDir = SiaDir("home", null)
        rootDir.addSiaFile(SiaFile("really/long/file/path/because/testing/file.txt", BigDecimal("498259")))
        rootDir.addSiaFile(SiaFile("people/kenzie/heart", BigDecimal("11616000000")))
        rootDir.addSiaFile(SiaFile("people/nick/life.txt", BigDecimal("847")))
        rootDir.addSiaFile(SiaFile("people/jeff/panda.png", BigDecimal("10567219")))
        rootDir.addSiaFile(SiaFile("colors/red.png", BigDecimal("48182")))
        rootDir.addSiaFile(SiaFile("colors/blue.jpg", BigDecimal("79")))
        rootDir.addSiaFile(SiaFile("colors/purple.rgb", BigDecimal("79")))
        callback.onSuccess?.invoke(rootDir)
    }
}