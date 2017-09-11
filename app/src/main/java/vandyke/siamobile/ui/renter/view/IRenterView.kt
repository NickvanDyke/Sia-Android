/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view

import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.backend.networking.SiaError

interface IRenterView {
    fun changeDisplayedDir(oldDir: SiaDir, newDir: SiaDir)
    fun onError(error: SiaError)
}