package com.vandyke.sia.data.local.daos;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import com.vandyke.sia.data.local.models.renter.Dir;

import java.math.BigDecimal;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface DirDao {
    @RawQuery(observedEntities = Dir.class)
    Flowable<List<Dir>> customQuery(final SupportSQLiteQuery query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReplaceOnConflict(Dir dir);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAbortOnConflict(Dir dir);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertIgnoreOnConflict(Dir dir);

    @Query("UPDATE dirs SET path = REPLACE(SUBSTR(path, 0, LENGTH(:path) + 1), :path, :newPath) || SUBSTR(path, LENGTH(:path) + 1) WHERE path == :path OR path LIKE :path || '/%'")
    void updatePath(String path, String newPath);

    @Query("UPDATE dirs SET size = :newSize WHERE path == :path")
    void updateSize(String path, BigDecimal newSize);

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

    @Query("DELETE FROM dirs WHERE path = :path")
    void deleteDir(String path);

    @Query("DELETE FROM dirs WHERE path LIKE :path || '/%'")
    void deleteDirsUnder(String path);
}
