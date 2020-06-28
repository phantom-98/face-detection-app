package com.facecoolalert.database.Repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.facecoolalert.database.entities.CrashLog;

import java.util.List;

@Dao
public interface CrashLogDao {

    @Insert
    void insertCrashLog(CrashLog crashLog);

    @Query("SELECT * FROM CrashLog WHERE uid = :uid")
    CrashLog getCrashLogByNum(String uid);

    @Query("SELECT * FROM CrashLog order by date desc")
    List<CrashLog> getAllCrashLogs();

    @Update
    void updateCrashLog(CrashLog crashLog);

    @Delete
    void delete(CrashLog crashLog);

    @Query("SELECT title FROM CrashLog order by date desc")
    List<String> listTitles();
}
