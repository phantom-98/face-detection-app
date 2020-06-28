package com.facecoolalert.database.Repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.facecoolalert.database.entities.DistributionList;

import java.util.List;

@Dao
public interface DistributionListDao {

    @Insert
    void insertDistributionList(DistributionList distributionList);

    @Update
    void updateDistributionList(DistributionList distributionList);

    @Query("SELECT * FROM DistributionList")
    List<DistributionList> getAllDistributionLists();

    @Query("SELECT * FROM DistributionList WHERE uid = :listId")
    DistributionList getDistributionListById(String listId);

    @Query("select name from DistributionList order by created asc")
    List<String> listNames();

    @Delete
    void delete(DistributionList distributionList);

    @Query("SELECT * FROM DistributionList WHERE name like :name")
    DistributionList getByName(String name);


}
