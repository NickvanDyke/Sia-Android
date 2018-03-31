package com.vandyke.sia.data.models.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "seeds")
data class SeedData(@PrimaryKey
                val seed: String)
