package com.vandyke.sia.data.local.daos

import android.arch.persistence.db.SimpleSQLiteQuery
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.repository.FilesRepository
import io.reactivex.Flowable

/* Reason for this class, and FileDao and DirDao being Java files: When they were Kotlin files, Room
 * would generate the @RawQuery code in such a way that caused compilation errors. Using Java files instead fixed it.
 * TODO: change back to Kotlin files once it's fixed. */

fun DirDao.getDirs(path: String, name: String? = null, orderBy: FilesRepository.OrderBy? = null, ascending: Boolean = true): Flowable<List<Dir>> {
    var query = when {
        path.isEmpty() && name?.isEmpty() == true -> "SELECT * FROM dirs WHERE path != ''"
        path.isNotEmpty() && name?.isEmpty() == true -> "SELECT * FROM dirs WHERE path LIKE '$path/%'"
        path.isNotEmpty() && name != null -> "SELECT * FROM dirs WHERE path LIKE '$path/%$name%' AND path NOT LIKE '$path/%$name%/%'"
        path.isNotEmpty() && name == null -> "SELECT * FROM dirs WHERE path LIKE '$path/%' AND path NOT LIKE '$path/%/%'"
        path.isEmpty() && name == null -> "SELECT * FROM dirs WHERE path NOT LIKE '%/%' AND path != ''"
        path.isEmpty() && name != null -> "SELECT * FROM dirs WHERE ((path LIKE '$path/%$name%' AND path NOT LIKE '$path/%$name%/%') OR (path LIKE '%$name%' AND path NOT LIKE '%$name%/%')) AND path != ''"
        else -> throw IllegalArgumentException()
    }

    if (orderBy != null) { // maybe include secondary sorting
        query += " ORDER BY ${orderBy.text}"
        if (!ascending) {
            query += " DESC"
        }
    }
    println("dirs query is: $query")

    return customQuery(SimpleSQLiteQuery(query))
}

fun FileDao.getFiles(path: String, name: String? = null, orderBy: FilesRepository.OrderBy? = null, ascending: Boolean = true): Flowable<List<RenterFileData>> {
    var query = when {
        path.isEmpty() && name?.isEmpty() == true -> "SELECT * FROM files"
        path.isNotEmpty() && name?.isEmpty() == true -> "SELECT * FROM files WHERE path LIKE '$path/%'"
        path.isNotEmpty() && name != null -> "SELECT * FROM files WHERE path LIKE '$path/%$name%' AND path NOT LIKE '$path/%$name%/%'"
        path.isNotEmpty() && name == null -> "SELECT * FROM files WHERE path LIKE '$path/%' AND path NOT LIKE '$path/%/%'"
        path.isEmpty() && name == null -> "SELECT * FROM files WHERE path NOT LIKE '%/%'"
        path.isEmpty() && name != null -> "SELECT * FROM files WHERE ((path LIKE '$path/%$name%' AND path NOT LIKE '$path/%$name%/%') OR (path LIKE '%$name%' AND path NOT LIKE '%$name%/%'))"
        else -> throw IllegalArgumentException()
    }

    if (orderBy != null) { // maybe include secondary sorting
        query += " ORDER BY ${orderBy.text}"
        if (!ascending) {
            query += " DESC"
        }
    }
    println("files query is: $query")

    return customQuery(SimpleSQLiteQuery(query))
}