package com.facecoolalert.database.Repositories;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;


import com.facecoolalert.database.entities.RecognitionResult;

import java.util.List;

@Dao
public interface RecognitionResultDao {

    @Insert
    void insertRecognitionResult(RecognitionResult result);

    @Query("SELECT * FROM RecognitionResult WHERE uid = :uid")
    RecognitionResult getByUid(String uid);

    @Query("SELECT bmp FROM RecognitionResult WHERE uid = :uid")
    byte[] getByteImage(String uid);

    @Query("update RecognitionResult set bmp=:bmp,features=:features WHERE uid = :uid")
    void updateByteImage(String uid,byte[] bmp,byte[] features);

    @Query("update RecognitionResult set bmp=:bmp,features=:features,lastChange=:changeTime WHERE uid = :uid")
    void restoreByteImage(String uid,byte[] bmp,byte[] features,Long changeTime);


    @Query("SELECT uid FROM RecognitionResult where bmp is not null order by date asc,lastChange asc limit :limit offset :offset")
    List<String> getEarlyRecognitionResults(int limit,int offset);

    @Query("SELECT * FROM RecognitionResult")
    List<RecognitionResult> getAllRecognitionResults();

    @Query("SELECT * FROM RecognitionResult order by date desc limit :limit")
    List<RecognitionResult> getAllRecognitionResult(int limit);

    @Query("SELECT uid FROM RecognitionResult order by date desc limit :limit")
    List<String> getRecentUids(int limit);



    @Query("SELECT * FROM RecognitionResult where subjectId = :subjectId order by date desc")
    List<RecognitionResult> getRecognitionResultsBySubject(Long subjectId);

    @Query("SELECT * FROM RecognitionResult order by date desc")
    List<RecognitionResult> getAllRecognitionResultsDesc();

    @Query("select count(*) from RecognitionResult where date <= :date")
    int countUpTo(Long date);



    @RawQuery
    int countRecognitionResultsByQuery(SupportSQLiteQuery query);

    @RawQuery
    List<RecognitionResult> getRecognitionResultsByQuery(SupportSQLiteQuery query);

    @RawQuery
    RecognitionResult getRecognitionResultByQuery(SupportSQLiteQuery query);

    @RawQuery
    List<String> getResultsUidsByQuery(SupportSQLiteQuery query);


    @Query("select location from RecognitionResult where location is not null group by location")
    LiveData<List<String>> listLocations();


    @Query("select date from RecognitionResult where subjectId=:subjectid and uid in (select recognitionResult_id from AlertLog) order by date desc limit 1")
    Long getLatestAlertforSubject(String subjectid);

    @Query("delete from RecognitionResult")
    void deleteAll();


    @Query("SELECT CAST(SUM(length(bmp)+length(features)) AS REAL) / 1024.0 / 1024.0 /1024.0 FROM RecognitionResult")
    Double totalBitmapsLength();


    @Query("SELECT * FROM RecognitionResult where bmp is not null and date>=:start and date<=:end order by date asc,lastChange asc limit :limit offset :offset")
    List<RecognitionResult> getDateRangeRecognitionResults(Long start,Long end,int limit,int offset);


    @Query("SELECT count(*) FROM RecognitionResult where bmp is not null and date>=:start and date<=:end")
    int getDateRangeRecognitionCount(long start, long end);


}
