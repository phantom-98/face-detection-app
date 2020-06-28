package com.facecoolalert.database.Repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.facecoolalert.database.entities.EnrollmentReport;
import com.facecoolalert.database.entities.Subject;

import java.util.List;

@Dao
public interface EnrollmentReportDao {

    @Insert
    void insert(EnrollmentReport enrollmentReport);

    @Query("SELECT * FROM EnrollmentReport WHERE uid = :uid")
    Subject getByUid(String uid);

    @Query("SELECT * FROM EnrollmentReport where date=:date")
    List<EnrollmentReport> getAll(Long date);

    @Query("SELECT date from EnrollmentReport group by date order by date desc")
    List<Long> listDates();


    @Query("SELECT count(*) FROM EnrollmentReport where date=:date")
    int countAll(Long date);

    @Query("SELECT count(*) FROM EnrollmentReport where date=:date and status like 'success%'")
    int countSuccess(Long date);

    @Update
    void update(EnrollmentReport enrollmentReport);

    @Delete
    void delete(EnrollmentReport enrollmentReport);

    @Query("delete from EnrollmentReport")
    void deleteAll();
}
