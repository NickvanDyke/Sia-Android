/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import com.vandyke.sia.data.models.renter.ContractData

@Dao
interface ContractDao : BaseDao<ContractData>