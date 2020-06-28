package com.facecoolalert.database.Repositories;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.facecoolalert.database.entities.Alert;

import java.util.List;

@Dao
public interface AlertDao {

    @Insert
    void insertAlert(Alert alert);

    @Query("SELECT * FROM Alert WHERE uid = :uid")
    Alert getAlertByNum(String uid);

    @Query("SELECT * FROM Alert")
    List<Alert> getAllAlerts();

    @Update
    void updateAlert(Alert alert);

    @Delete
    void delete(Alert alert);

    @Query("SELECT name FROM Alert order by created asc")
   List<String> listNames();

    @Query("SELECT * FROM Alert where watchlist_id=:watchlist_id and (location=:location or location='All')")
    List<Alert> getAllAlertsByWatchList(String watchlist_id,String location);

    @Query("delete from Alert where watchlist_id=:watchlist_id")
    void deleteByWatchList(String watchlist_id);

    @Query("delete from Alert where distributionList_id=:distList_id")
    void deleteByDistributionList(String distList_id);


}
