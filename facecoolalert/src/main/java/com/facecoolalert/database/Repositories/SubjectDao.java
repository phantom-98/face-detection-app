package com.facecoolalert.database.Repositories;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;


import com.facecoolalert.database.entities.Subject;

import java.util.List;

@Dao
public interface SubjectDao {

    @Insert
    void insertSubject(Subject subject);

    @Query("SELECT * FROM Subject WHERE uid = :uid")
    Subject getSubjectByuid(String uid);

    @Query("SELECT * FROM Subject order by firstName,lastName asc")
    List<Subject> getAllSubjects();

    @Query("SELECT firstName,lastName,ID,uid,watchlist FROM Subject order by firstName,lastName asc")
    List<Subject> listSubjects();

    @Update
    void updateSubject(Subject subject);

    @Delete
    void delete(Subject subject);

    @Query("select ID from subject where length(ID)>0  group by id order by ID asc")
    LiveData<List<String>> listIds();

    @Query("SELECT DISTINCT firstName || ' ' || lastName AS fullName FROM Subject where length(firstName || ' ' || lastName)>1 order by firstName,lastName asc")
    LiveData<List<String>> listNames();


    @Query("select count(*) from Subject where watchlist=:watchlist")
    int countByWatchList(String watchlist);

//    RecognitionResult getRecognitionResultByQuery(SupportSQLiteQuery query);
    @Transaction
    @RawQuery
     int updateWatchList(SupportSQLiteQuery query);


    @Query("delete from Subject")
    public void deleteAll();


    @Query("select count(*) from Subject where firstName=:firstName and lastName=:lastName")
    int countByName(String firstName,String lastName);

    @Query("select count(*) from Subject where id=:id")
    int countByID(String id);



}
