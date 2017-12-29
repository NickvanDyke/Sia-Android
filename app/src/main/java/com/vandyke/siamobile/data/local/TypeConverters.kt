/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.local

import android.arch.persistence.room.TypeConverter
import java.math.BigDecimal

class TypeConverters {
    @TypeConverter
    fun toString(bigDecimal: BigDecimal) = bigDecimal.toPlainString()

    @TypeConverter
    fun fromString(string: String) = BigDecimal(string)
}