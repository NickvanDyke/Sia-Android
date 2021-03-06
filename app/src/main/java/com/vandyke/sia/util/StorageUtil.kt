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
    fun readableFilesizeString(filesize: Long): String {
        var size = filesize.toDouble()
        var i = 0
        val kilo = 1000
        while (size > kilo && i < 6) {
            size /= kilo
            i++
        }
        val sizeString = when (i) {
            0 -> "B"
            1 -> "KB"
            2 -> "MB"
            3 -> "GB"
            4 -> "TB"
            5 -> "PB"
            6 -> "EB"
            else -> "Super big"
        }

        return "${size.format()} $sizeString"
    }

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

    /** This isn't actually in use anymore. It's still here solely for updating apps that used the
     *  external storage dir returned by this, to the new method */
    fun getExternalStorage(context: Context): File {
        val dirs = context.getExternalFilesDirs(null)
        if (dirs.isEmpty())
            throw ExternalStorageException("No external storage available")

        /* dirs[1] will be removable storage, which we prefer over emulated external storage, which dirs[0] will be */
        val dir = if (dirs.size > 1) dirs[1] else dirs[0]

        val state = Environment.getExternalStorageState(dir)
        if (state != Environment.MEDIA_MOUNTED)
            throw ExternalStorageException("External storage error: $state")

        return dir
    }
}

fun Context.getAllFilesDirs(): List<File> = mutableListOf(filesDir).apply { addAll(getExternalFilesDirs(null)) }.filterNotNull()

class ExternalStorageException(msg: String) : Exception(msg)

fun File.recursiveLength(): Long {
    var size = this.length()
    this.listFiles()?.forEach {
        size += if (it.isDirectory)
            it.recursiveLength()
        else
            it.length()
    }
    return size
}