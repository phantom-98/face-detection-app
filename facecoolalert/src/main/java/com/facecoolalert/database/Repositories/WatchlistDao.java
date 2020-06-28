package com.facecoolalert.database.Repositories;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.facecoolalert.database.entities.WatchList;

import java.util.List;

@Dao
public interface WatchlistDao {

    @Insert
    void insertWatchlist(WatchList watchList);

    @Query("SELECT * FROM WatchList order by createdOn asc")
    List<WatchList> getAll();

    @Query("SELECT name FROM WatchList ORDER BY CASE WHEN name = 'Default' THEN 0 ELSE 1 END, createdOn ASC")
    List<String> listAll();



    @Query("SELECT * FROM WatchList WHERE uid = :uid")
    WatchList getByNum(String uid);



    @Update
    void update(WatchList watchList);

    @Delete
    void delete(WatchList watchList);

    @Query("SELECT COUNT(*) FROM WatchList WHERE name = :watchlist")
    int watchlists(String watchlist);

    @Query("SELECT * FROM WatchList WHERE name like :name")
    WatchList getByName(String name);



}
