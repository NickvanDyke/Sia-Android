package com.vandyke.sia.data.local.daos;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import com.vandyke.sia.data.local.models.renter.Dir;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public abstract class DirDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertReplaceIfConflict(Dir dir);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public abstract void insertAbortIfConflict(Dir dir);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertIgnoreIfConflict(Dir dir);

    @Query("UPDATE dirs SET path = :newPath WHERE path == :path")
    public abstract void updatePath(String path, String newPath);

    @Query("SELECT * FROM dirs")
    public abstract Flowable<List<Dir>> all();

    @Query("SELECT * FROM dirs")
    public abstract Single<List<Dir>> getAll();

    @Query("SELECT * FROM dirs WHERE path = :path")
    public abstract Flowable<Dir> dir(String path);

    // TODO: should maybe return a Maybe instead of a Single? Since I don't want an error when it's an empty result
    @Query("SELECT * FROM dirs WHERE path = :path")
    public abstract Single<Dir> getDir(String path);

    @RawQuery(observedEntities = Dir.class)
    public abstract Flowable<List<Dir>> customQuery(final SupportSQLiteQuery query);

    @Query("DELETE FROM dirs")
    public abstract void deleteAll();

    @Query("DELETE FROM dirs WHERE path = :path")
    public abstract void deleteDir(String path);

    @Query("DELETE FROM dirs WHERE path LIKE :path || '/%'")
    public abstract void deleteDirsUnder(String path);
}
