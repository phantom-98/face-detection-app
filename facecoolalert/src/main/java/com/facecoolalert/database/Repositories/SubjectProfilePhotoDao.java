package com.facecoolalert.database.Repositories;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.facecoolalert.database.entities.SubjectProfilePhoto;

import java.util.List;
import java.util.Optional;

@Dao

public interface SubjectProfilePhotoDao {




    @Query("select * from SubjectProfilePhoto s where s.uid=:subjectId")
    Optional<SubjectProfilePhoto> findBySubjectId(String subjectId);

    @Query("select * from SubjectProfilePhoto")
    List<SubjectProfilePhoto> listAll();

    @Update
    void update(SubjectProfilePhoto photo);

    @Insert
    void insert(SubjectProfilePhoto photo);


    @Query("delete from SubjectProfilePhoto")
    void deleteAll();

    @Query("select * from SubjectProfilePhoto order by uid asc limit :fetchSize offset :offset")
    List<SubjectProfilePhoto> fetchRange(int offset, int fetchSize);
}
