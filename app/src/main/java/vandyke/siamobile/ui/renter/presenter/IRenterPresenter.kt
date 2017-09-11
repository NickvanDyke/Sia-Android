/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.presenter

import vandyke.siamobile.backend.data.renter.SiaDir

interface IRenterPresenter {
    fun refresh()
    fun changeDir(dir: SiaDir)
    fun goUp(num: Int)
}