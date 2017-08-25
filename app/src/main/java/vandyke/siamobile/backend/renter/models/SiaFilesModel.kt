/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.renter.models

import vandyke.siamobile.backend.renter.SiaFile

data class SiaFilesModel(val files: ArrayList<SiaFile> = ArrayList())