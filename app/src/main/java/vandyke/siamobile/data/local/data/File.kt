/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.data

import android.arch.persistence.room.Entity

@Entity(tableName = "files")
class File(path: String) : Node(path)