/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object StorageUtil {

    fun copyFromAssetsToAppStorage(filename: String, context: Context): File? {
        try {
            val inputStream = context.assets.open(filename)
            val result = File(context.filesDir, filename)
            if (result.exists())
                return result
            val out = FileOutputStream(result)
            val buf = ByteArray(1024)
            var length: Int = inputStream.read(buf)
            while (length > 0) {
                out.write(buf, 0, length)
                length = inputStream.read(buf)
            }
            result.setExecutable(true)
            inputStream.close()
            out.close()
            return result
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun externalStorageStateDescription(): String {
        return when (Environment.getExternalStorageState()) {
            Environment.MEDIA_BAD_REMOVAL -> "External storage was previously removed before being unmounted"
            Environment.MEDIA_CHECKING -> "External storage is present but being disk-checked"
            Environment.MEDIA_EJECTING -> "External storage is in the process of ejecting"
            Environment.MEDIA_MOUNTED -> "External storage is present and mounted with read/write access"
            Environment.MEDIA_MOUNTED_READ_ONLY -> "External storage is present but mounted as read-only"
            Environment.MEDIA_NOFS -> "External storage is present but is blank or using an unsupported filesystem"
            Environment.MEDIA_REMOVED -> "External storage is not present"
            Environment.MEDIA_SHARED -> "External storage is present but being shared via USB"
            Environment.MEDIA_UNKNOWN -> "External storage is in an unknown state"
            Environment.MEDIA_UNMOUNTABLE -> "External storage is present but cannot be mounted. May be corrupted"
            Environment.MEDIA_UNMOUNTED -> "External storage is present but unmounted"
            else -> "Error with external storage"
        }
    }
}