package com.facecoolalert.database.Repositories;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.facecoolalert.database.entities.AlertLog;


import java.util.List;

@Dao
public interface AlertLogDao {

    @Insert
    void insertAlertLog(AlertLog alertLog);

    @Query("SELECT * FROM AlertLog WHERE uid = :uid")
    AlertLog getAlertLogByNum(String uid);

    @Query("SELECT a.* FROM AlertLog a join RecognitionResult r on a.recognitionResult_id=r.uid order by r.date desc")
    List<AlertLog> getAllAlertLogs();

    @Update
    void updateAlertLog(AlertLog alertLog);

    @Delete
    void delete(AlertLog alertLog);

    @Query("select count(*) from AlertLog")
    int countAll();

    @Query("SELECT a.* FROM AlertLog a join RecognitionResult r on a.recognitionResult_id=r.uid order by r.date desc limit 1 offset :position")
    AlertLog getAlertLogByPosition(Long position);


    @Query("delete from AlertLog")
    void deleteAll();
}
