package com.facecoolalert.database.Repositories;


import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

@Dao
public interface DatabaseDao {


    @RawQuery
    int checkpoint(SupportSQLiteQuery supportSQLiteQuery);


}
