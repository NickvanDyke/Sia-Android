package vandyke.siamobile.ui.renter.model

import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.backend.networking.SiaCallback

interface IRenterModel {
    fun getRootDir(callback: SiaCallback<SiaDir>)
}