package com.facecoolalert.database.Repositories;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.facecoolalert.database.entities.DistributionList;
import com.facecoolalert.database.entities.Subscriber;
import com.facecoolalert.database.entities.SubscriberDistributionListCrossRef;

import java.util.List;

@Dao
public interface SubscriberDistributionListDao {
    @Insert
    void insert(SubscriberDistributionListCrossRef subscriberDistributionListCrossRef);

    @Query("SELECT * FROM DistributionList " +
           "INNER JOIN SubscriberDistributionListCrossRef ON " +
           "DistributionList.uid = SubscriberDistributionListCrossRef.distributionList_id " +
           "WHERE SubscriberDistributionListCrossRef.subscriber_id = :subscriberId")
    List<DistributionList> getDistributionListsForSubscriber(String subscriberId);


    @Query("SELECT Subscriber.* FROM Subscriber " +
            "INNER JOIN SubscriberDistributionListCrossRef ON " +
            "Subscriber.uid = SubscriberDistributionListCrossRef.subscriber_id " +
            "WHERE SubscriberDistributionListCrossRef.distributionList_id = :distributionlist")
    List<Subscriber> getSubscribersForList(String distributionlist);

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertOrUpdateSubscribersToDistributionList(Long distributionListId, List<Subscriber> subscribers);

    @Query("delete from SubscriberDistributionListCrossRef where distributionList_id=:distributionListId")
    void deleteDistributionListS(String distributionListId);


    @Query("select count(*) from SubscriberDistributionListCrossRef where distributionList_id=:distributionList")
    int countSubscribers(String distributionList);


}
