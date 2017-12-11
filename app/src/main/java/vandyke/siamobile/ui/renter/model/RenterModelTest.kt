/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.model

import io.reactivex.Single
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.data.renter.SiaFile
import java.math.BigDecimal

class RenterModelTest : IRenterModel {
    override fun getRootDir(): Single<SiaDir> {
        val rootDir = SiaDir("home", null)
        rootDir + SiaFile("really/long/file/path/because/testing/file.txt", BigDecimal("498259"))
        rootDir + SiaFile("people/kenzie/heart", BigDecimal("116160000000000000000"))
        rootDir + SiaFile("people/nick/life.txt", BigDecimal("847"))
        rootDir + SiaFile("people/jeff/panda.png", BigDecimal("10567219"))
        rootDir + SiaFile("colors/red.png", BigDecimal("48182"))
        rootDir + SiaFile("colors/blue.jpg", BigDecimal("6949"))
        rootDir + SiaFile("colors/purple.pdf", BigDecimal("79"))
        rootDir + SiaFile("colors/bright/orange.rgb", BigDecimal("23583"))
        return Single.just(rootDir)
    }
}