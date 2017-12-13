/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.model

import io.reactivex.Completable
import io.reactivex.Single
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.data.renter.SiaFile
import vandyke.siamobile.data.local.Prefs
import vandyke.siamobile.data.remote.SiaError
import vandyke.siamobile.data.remote.siaApi
import vandyke.siamobile.ui.renter.view.RenterFragment.Companion.ROOT_DIR_NAME
import java.math.BigDecimal

class RenterModelTest : IRenterModel {
    private val files = mutableListOf(SiaFile("really/long/file/path/because/testing/file.txt", BigDecimal("498259")),
            SiaFile("people/jamison/bro", BigDecimal("116160000000000000000")),
            SiaFile("people/nick/life.txt", BigDecimal("847")),
            SiaFile("people/jeff/panda.png", BigDecimal("10567219")),
            SiaFile("colors/red.png", BigDecimal("48182")),
            SiaFile("colors/blue.jpg", BigDecimal("6949")),
            SiaFile("colors/purple.pdf", BigDecimal("79")),
            SiaFile("colors/bright/orange.rgb", BigDecimal("23583")))

    override fun getRootDir(): Single<SiaDir> {
        // TODO: sort somehow. particularly when integrating locally-created dirs with ones returned from the sia node. Probably by name
        // or by size (or even last modified date if I ever keep that locally). Should have filter setting in fragment
        val rootDir = SiaDir(ROOT_DIR_NAME, null)
        Prefs.renterDirs.forEach {
            rootDir.addEmptySiaDirAtPath(it.split("/"))
        }
        files.forEach {
            rootDir.addSiaFile(it)
        }
        return Single.just(rootDir)
    }

    override fun createNewDir(path: String): Completable {
        return Completable.create {
            if (Prefs.renterDirs.add(path))
                it.onComplete()
            else
                it.onError(SiaError(SiaError.Reason.DIRECTORY_ALREADY_EXISTS))
        }
    }

    override fun deleteDir(dir: SiaDir) {
        Prefs.renterDirs.remove(dir.fullPathString.replace("$ROOT_DIR_NAME/", ""))
        dir.directories.forEach {
            deleteDir(it)
        }
        dir.files.forEach {
            deleteFile(it)
        }
    }

    override fun deleteFile(file: SiaFile): Completable {
        files.remove(file)
        return siaApi.renterDelete(file.siapath)
    }
}