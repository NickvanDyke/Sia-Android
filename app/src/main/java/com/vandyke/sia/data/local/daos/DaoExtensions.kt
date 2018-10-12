package com.vandyke.sia.data.local.daos

import androidx.sqlite.db.SimpleSQLiteQuery
import com.vandyke.sia.data.models.renter.Dir
import com.vandyke.sia.data.models.renter.SiaFile
import com.vandyke.sia.data.repository.FilesRepository.OrderBy
import io.reactivex.Flowable
import io.reactivex.Single

/* Reason for this class, and FileDao and DirDao being Java files: When they were Kotlin files, Room
 * would generate the @RawQuery code in such a way that caused compilation errors. Using Java files instead fixed it.
 * TODO: change back to Kotlin files once it's fixed. */

fun DirDao.dirsUnderDir(dirPath: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): Flowable<List<Dir>> {
    return customQueryFlowable(SimpleSQLiteQuery(underDirQuery("dirs", dirPath, orderBy, ascending)))
}

fun DirDao.dirsUnderDirWithName(dirPath: String, name: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): Flowable<List<Dir>> {
    return customQueryFlowable(SimpleSQLiteQuery(underDirWithNameQuery("dirs", dirPath, name, orderBy, ascending)))
}

fun DirDao.dirsInDir(dirPath: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): Flowable<List<Dir>> {
    return customQueryFlowable(SimpleSQLiteQuery(inDirQuery("dirs", dirPath, orderBy, ascending)))
}

/* Not sure why, but using firstOrError() on filesUnderDir never emits, so have a Single version of it instead */
fun FileDao.getFilesUnderDir(dirPath: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): Single<List<SiaFile>> {
    return customQuerySingle(SimpleSQLiteQuery(underDirQuery("files", dirPath, orderBy, ascending)))
}

fun FileDao.filesUnderDir(dirPath: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): Flowable<List<SiaFile>> {
    return customQueryFlowable(SimpleSQLiteQuery(underDirQuery("files", dirPath, orderBy, ascending)))
}

fun FileDao.filesUnderDirWithName(dirPath: String, name: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): Flowable<List<SiaFile>> {
    return customQueryFlowable(SimpleSQLiteQuery(underDirWithNameQuery("files", dirPath, name, orderBy, ascending)))
}

fun FileDao.filesInDir(dirPath: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): Flowable<List<SiaFile>> {
    return customQueryFlowable(SimpleSQLiteQuery(inDirQuery("files", dirPath, orderBy, ascending)))
}


private fun inDirQuery(tableName: String, dirPath: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): String {
    var query = if (dirPath.isEmpty())
        "SELECT * FROM $tableName WHERE path NOT LIKE '%/%'"
    else
        "SELECT * FROM $tableName WHERE path LIKE '$dirPath/%' AND path NOT LIKE '$dirPath/%/%'"

    // maybe include secondary sorting
    query += " ORDER BY ${orderBy.text}"
    if (!ascending)
        query += " DESC"

    return query
}

private fun underDirQuery(tableName: String, dirPath: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): String {
    var query = "SELECT * FROM $tableName"
    if (dirPath.isNotEmpty())
        query += " WHERE path LIKE '$dirPath/%'"

    query += " ORDER BY ${orderBy.text}"
    if (!ascending)
        query += " DESC"

    return query
}

private fun underDirWithNameQuery(tableName: String, dirPath: String, name: String, orderBy: OrderBy = OrderBy.PATH, ascending: Boolean = true): String {
    var query = when {
        dirPath.isEmpty() && name.isEmpty() -> "SELECT * FROM $tableName"
        dirPath.isNotEmpty() && name.isEmpty() -> "SELECT * FROM $tableName WHERE path LIKE '$dirPath/%'"
        dirPath.isNotEmpty() -> "SELECT * FROM $tableName WHERE path LIKE '$dirPath/%$name%' AND path NOT LIKE '$dirPath/%$name%/%'"
        dirPath.isEmpty() -> "SELECT * FROM $tableName WHERE ((path LIKE '$dirPath/%$name%' AND path NOT LIKE '$dirPath/%$name%/%') OR (path LIKE '%$name%' AND path NOT LIKE '%$name%/%'))"
        else -> throw IllegalArgumentException()
    }

    query += " ORDER BY ${orderBy.text}"
    if (!ascending)
        query += " DESC"

    return query
}
