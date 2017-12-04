package vandyke.siamobile.ui.renter.model

import io.reactivex.Single
import vandyke.siamobile.backend.data.renter.SiaDir

interface IRenterModel {
    fun getRootDir(): Single<SiaDir>
}