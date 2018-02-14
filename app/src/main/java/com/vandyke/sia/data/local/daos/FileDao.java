package com.vandyke.sia.data.local.daos;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import com.vandyke.sia.data.models.renter.RenterFileData;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface FileDao extends BaseDao<RenterFileData> {
    @RawQuery(observedEntities = RenterFileData.class)
    Flowable<List<RenterFileData>> customQuery(final SupportSQLiteQuery query);

    @Query("UPDATE files SET path = REPLACE(SUBSTR(path, 0, LENGTH(:path) + 1), :path, :newPath) || SUBSTR(path, LENGTH(:path) + 1) WHERE path == :path")
    void updatePath(String path, String newPath);

    @Query("SELECT * FROM files")
    Single<List<RenterFileData>> getAll();

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%'")
    Single<List<RenterFileData>> getFilesUnder(String path);

    @Query("DELETE FROM files")
    void deleteAll();
}
