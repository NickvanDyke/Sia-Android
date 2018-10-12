package com.vandyke.sia.data.local.daos;

import com.vandyke.sia.data.models.renter.SiaFile;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface FileDao extends BaseDao<SiaFile> {
    @RawQuery(observedEntities = SiaFile.class)
    Flowable<List<SiaFile>> customQueryFlowable(final SupportSQLiteQuery query);

    @RawQuery(observedEntities = SiaFile.class)
    Single<List<SiaFile>> customQuerySingle(final SupportSQLiteQuery query);

    // is this doing something different than just setting path to newPath...? don't remember from when I made it. Surely it must be, otherwise I would've just done that
    @Query("UPDATE files SET path = REPLACE(SUBSTR(path, 0, LENGTH(:path) + 1), :path, :newPath) || SUBSTR(path, LENGTH(:path) + 1) WHERE path == :path")
    void updatePath(String path, String newPath);

    @Query("SELECT * FROM files ORDER BY path")
    Single<List<SiaFile>> getAllByPath();

    @Query("DELETE FROM files")
    void deleteAll();
}
