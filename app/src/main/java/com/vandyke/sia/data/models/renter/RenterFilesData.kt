/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

data class RenterFilesData(val files: List<SiaFile>?) /* Sia API response won't contain a files field if it has no files, so it's nullable */