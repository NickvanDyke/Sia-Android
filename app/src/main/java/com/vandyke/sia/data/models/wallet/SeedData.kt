package com.vandyke.sia.data.models.wallet

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seeds")
data class SeedData(@PrimaryKey
                val seed: String)
