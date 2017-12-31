/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.data.renter

import android.arch.persistence.room.Entity

@Entity(tableName = "files")
class File(path: String) : Node(path)