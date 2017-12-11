package vandyke.siamobile.ui.renter.model

import io.reactivex.Single
import vandyke.siamobile.data.data.renter.SiaDir

interface IRenterModel {
    fun getRootDir(): Single<SiaDir>
}