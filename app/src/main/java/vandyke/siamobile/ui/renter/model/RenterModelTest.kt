/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.model

import io.reactivex.Single
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.data.renter.SiaFile
import vandyke.siamobile.data.local.Prefs
import vandyke.siamobile.ui.renter.view.RenterFragment.Companion.ROOT_DIR_NAME
import java.math.BigDecimal

class RenterModelTest : IRenterModel {
    override fun getRootDir(): Single<SiaDir> {
        // TODO: sort somehow. particularly when integrating locally-created dirs with ones returned from the sia node
        val rootDir = SiaDir(ROOT_DIR_NAME, null)
        Prefs.renterDirs.forEach {
            println(it)
            rootDir.addEmptySiaDirAtPath(it.split("/"))
        }
        rootDir.addSiaFile(SiaFile("really/long/file/path/because/testing/file.txt", BigDecimal("498259")))
        rootDir.addSiaFile(SiaFile("people/jamison/bro", BigDecimal("116160000000000000000")))
        rootDir.addSiaFile(SiaFile("people/nick/life.txt", BigDecimal("847")))
        rootDir.addSiaFile(SiaFile("people/jeff/panda.png", BigDecimal("10567219")))
        rootDir.addSiaFile(SiaFile("colors/red.png", BigDecimal("48182")))
        rootDir.addSiaFile(SiaFile("colors/blue.jpg", BigDecimal("6949")))
        rootDir.addSiaFile(SiaFile("colors/purple.pdf", BigDecimal("79")))
        rootDir.addSiaFile(SiaFile("colors/bright/orange.rgb", BigDecimal("23583")))
        rootDir.printAll()
        return Single.just(rootDir)
    }

    override fun createNewDir(path: String) {
        Prefs.renterDirs.add(path) // TODO: changes to Prefs sets aren't persisting
    }
}