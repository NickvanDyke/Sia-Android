/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.presenter

import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.ui.renter.model.IRenterModel
import vandyke.siamobile.ui.renter.view.IRenterView

class RenterPresenter(val view: IRenterView, val model: IRenterModel) : IRenterPresenter {

    private var currentDir = SiaDir("Home", null)
        set(value) {
            view.changeDisplayedDir(field, value)
            field = value
        }

    override fun refresh() {
        refreshFiles()
    }

    fun refreshFiles() {
        model.getRootDir(SiaCallback({ it ->
            currentDir = it
        }, {
            view.onError(it)
        }))
    }

    override fun changeDir(dir: SiaDir) {
        currentDir = dir
    }

    override fun goUp(num: Int) {
        currentDir = currentDir.getParentDirAt(num)
    }
}