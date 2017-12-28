/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.repository

import vandyke.siamobile.data.remote.siaApi
import vandyke.siamobile.db

class ScValueRepository {
    fun updateScValue() = siaApi.getScPrice().doAfterSuccess {
        db.scValueDao().insert(it)
    }.toCompletable()

    fun scValue() = db.scValueDao().mostRecent()
}