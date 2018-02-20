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

    fun getExternalStorage(context: Context): File {
        val dirs = context.getExternalFilesDirs(null)
        if (dirs.isEmpty())
            throw ExternalStorageError("No external storage available")

        /* dirs[1] will be removable storage, which we prefer over emulated external storage, which dirs[0] will be */
        val dir = if (dirs.size > 1) dirs[1] else dirs[0]

        val state = Environment.getExternalStorageState(dir)
        if (state != Environment.MEDIA_MOUNTED)
            throw ExternalStorageError("External storage error: $state")

        return dir
    }
}

class ExternalStorageError(msg: String) : Exception(msg)