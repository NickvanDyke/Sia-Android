package vandyke.siamobile.ui.renter.presenter

import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.ui.renter.model.IRenterModel
import vandyke.siamobile.ui.renter.view.IRenterView

class RenterPresenter(val view: IRenterView, val model: IRenterModel) : IRenterPresenter {

    override fun refresh() {
        refreshFiles()
    }

    fun refreshFiles() {
        model.getRootDir(SiaCallback({ it ->
            view.onRootDirUpdate(it)
        }, {
            view.onError(it)
        }))
    }
}