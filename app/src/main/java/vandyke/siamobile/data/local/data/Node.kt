/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.data

import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

open class Node(path: String) {

    /* ensure that the path always starts with a / */
    @PrimaryKey
    var path = if (path.startsWith('/')) path else "/" + path

    @Ignore
    val name = this.path.substring(this.path.lastIndexOf('/') + 1)

    @Ignore
    val parent = this.path.substring(0, this.path.lastIndexOf('/'))
}