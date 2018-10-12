package com.vandyke.sia.data.local.daos;

import com.vandyke.sia.data.models.renter.Dir;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface DirDao extends BaseDao<Dir> {
    @RawQuery(observedEntities = Dir.class)
    Flowable<List<Dir>> customQueryFlowable(final SupportSQLiteQuery query);

    @Query("UPDATE dirs SET path = REPLACE(SUBSTR(path, 0, LENGTH(:path) + 1), :path, :newPath) || SUBSTR(path, LENGTH(:path) + 1) WHERE path == :path OR path LIKE :path || '/%'")
    void updatePath(String path, String newPath);

    @Query("UPDATE dirs SET size = :newSize WHERE path == :path")
    void updateSize(String path, Long newSize);

    @Query("SELECT * FROM dirs")
    Flowable<List<Dir>> all();

    @Query("SELECT * FROM dirs")
    Single<List<Dir>> getAll();

    @Query("SELECT * FROM dirs WHERE path == :path")
    Flowable<Dir> dir(String path);

    @Query("SELECT * FROM dirs WHERE path == :path")
    Single<Dir> getDir(String path);

    @Query("SELECT * FROM dirs WHERE INSTR(:filePath, path) == 1")
    Single<List<Dir>> getDirsContainingFile(String filePath);

    @Query("SELECT * FROM dirs WHERE path LIKE :dirPath || '/%'")
    Single<List<Dir>> getDirsUnder(String dirPath);

    @Query("DELETE FROM dirs")
    void deleteAll();

    @Query("DELETE FROM dirs WHERE path LIKE :path || '/%'")
    void deleteDirsUnder(String path);
}
