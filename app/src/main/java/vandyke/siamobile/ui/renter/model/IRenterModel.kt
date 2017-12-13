package vandyke.siamobile.ui.renter.model

import io.reactivex.Completable
import io.reactivex.Single
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.data.renter.SiaFile

interface IRenterModel {
    fun getRootDir(): Single<SiaDir>
    fun createNewDir(path: String): Completable
    fun deleteDir(dir: SiaDir)
    fun addFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int): Completable
    fun deleteFile(file: SiaFile): Completable
}