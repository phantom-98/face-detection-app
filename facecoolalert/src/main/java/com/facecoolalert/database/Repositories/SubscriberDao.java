package com.facecoolalert.database.Repositories;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.facecoolalert.database.entities.Subscriber;

import java.util.List;

@Dao
public interface SubscriberDao {

    @Insert
    void insertSubscriber(Subscriber subscriber);

    @Query("SELECT * FROM Subscriber WHERE uid = :uid")
    Subscriber getSubscriberByuid(Long uid);

    @Query("SELECT * FROM Subscriber")
    List<Subscriber> getAllSubscribers();

    @Update
    void updateSubscriber(Subscriber subscriber);

    @Delete
    void delete(Subscriber subscriber);

    @Query("SELECT name FROM Subscriber  where name is not null and length(name)>0 order by uid asc")
   List<String> listNames();


    @Query("SELECT * FROM Subscriber where name in (:list)")
    List<Subscriber> getSubscribers(List<String> list);

    @Query("delete from subscriberdistributionlistcrossref where subscriber_id=:distributionList_id")
    void deletefromDists(String distributionList_id);
}
