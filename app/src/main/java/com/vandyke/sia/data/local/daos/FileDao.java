package com.vandyke.sia.data.local.daos;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import com.vandyke.sia.data.models.renter.RenterFileData;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public abstract class FileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(RenterFileData file);

    @RawQuery(observedEntities = RenterFileData.class)
    public abstract Flowable<List<RenterFileData>> customQuery(final SupportSQLiteQuery query);

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%'")
    public abstract Single<List<RenterFileData>> getFilesUnder(String path);

    @Query("DELETE FROM files")
    public abstract void deleteAll();

    @Query("DELETE FROM files WHERE path == :path")
    public abstract void deleteFile(String path);

    @Query("DELETE FROM files WHERE path LIKE :path || '/%'")
    public abstract void deleteFilesUnder(String path);
}
