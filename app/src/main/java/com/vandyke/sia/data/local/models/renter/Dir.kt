/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.models.renter

import android.arch.persistence.room.Entity
import java.math.BigDecimal

@Entity(tableName = "dirs")
class Dir(path: String, size: BigDecimal) : Node(path, size)