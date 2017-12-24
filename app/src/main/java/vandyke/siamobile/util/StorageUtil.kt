/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import vandyke.siamobile.data.local.Prefs
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object StorageUtil {

    fun copyBinary(filename: String, context: Context): File? {
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

    fun getWorkingDirectory(context: Context?): File? {
        if (context == null)
            return null
        var result: File?
        if (Prefs.useExternal) {
            result = context.getExternalFilesDir(null)
            if (result == null) { // external storage not found
                Toast.makeText(context, "No external storage found. Using internal", Toast.LENGTH_LONG).show()
                result = context.filesDir
            }
        } else
            result = context.filesDir
        return result
    }

    val isExternalStorageWritable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun externalStorageStateDescription(): String {
        return when (Environment.getExternalStorageState()) {
            Environment.MEDIA_BAD_REMOVAL -> "external storage was previously removed before being unmounted"
            Environment.MEDIA_CHECKING -> "external storage is present but being disk-checked"
            Environment.MEDIA_EJECTING -> "external storage is in the process of ejecting"
            Environment.MEDIA_MOUNTED -> "external storage is present and mounted with read/write access"
            Environment.MEDIA_MOUNTED_READ_ONLY -> "external storage is present but mounted as read-only"
            Environment.MEDIA_NOFS -> "external storage is present but is blank or using an unsupported filesystem"
            Environment.MEDIA_REMOVED -> "external storage is not present"
            Environment.MEDIA_SHARED -> "external storage is present but being shared via USB"
            Environment.MEDIA_UNKNOWN -> "external storage is in an unknown state"
            Environment.MEDIA_UNMOUNTABLE -> "external storage is present but cannot be mounted. May be corrupted"
            Environment.MEDIA_UNMOUNTED -> "external storage is present but unmounted"
            else -> "external storage state missed all cases"
        }
    }
}