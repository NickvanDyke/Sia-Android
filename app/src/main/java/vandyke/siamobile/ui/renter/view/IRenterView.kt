package vandyke.siamobile.ui.renter.view

import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.backend.networking.SiaError

interface IRenterView {
    fun onRootDirUpdate(dir: SiaDir)
    fun onCurrentDirUpdate(dir: SiaDir)
    fun goUpDir(): Boolean
    fun onError(error: SiaError)
}