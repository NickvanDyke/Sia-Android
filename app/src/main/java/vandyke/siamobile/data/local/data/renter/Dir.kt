/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.data.renter

import android.arch.persistence.room.Entity

@Entity(tableName = "dirs")
class Dir(path: String) : Node(path)