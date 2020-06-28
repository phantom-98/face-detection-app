package com.facecoolalert.database.LongToStringMigration;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.facecoolalert.App;

public class LongToStringUidMigration {

    public static Migration getMigration()
    {
        return new Migration(20,21) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
//                        Rename existing tables to create new ones
                database.execSQL("alter table Subject rename to oldSubject ");
                database.execSQL("alter table RecognitionResult rename to oldRecognitionResult ");

                database.execSQL("alter table Subscriber rename to oldSubscriber ");
                database.execSQL("alter table WatchList rename to oldWatchList ");
                database.execSQL("alter table DistributionList rename to oldDistributionList ");
                database.execSQL("alter table SubscriberDistributionListCrossRef rename to oldSubscriberDistributionListCrossRef ");

                database.execSQL("alter table Alert rename to oldAlert ");
                database.execSQL("alter table AlertLog rename to oldAlertLog ");

                database.execSQL("alter table EnrollmentReport rename to oldEnrollmentReport ");
                database.execSQL("alter table CrashLog rename to oldCrashLog ");
                database.execSQL("alter table SubjectProfilePhoto rename to oldSubjectProfilePhoto ");

                //create the new tables
                database.execSQL("CREATE TABLE IF NOT EXISTS `Subject` (`uid` TEXT NOT NULL, `watchlist` TEXT, `firstName` TEXT, `lastName` TEXT, `ID` TEXT, `email` TEXT, `phone` TEXT, `address` TEXT, `features` BLOB, `imageQuality` REAL, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `RecognitionResult` (`uid` TEXT NOT NULL, `subjectId` TEXT, `date` INTEGER, `features` BLOB, `bmp` BLOB, `live` REAL NOT NULL, `scoreMatch` REAL NOT NULL, `location` TEXT, `camera` INTEGER NOT NULL, `lastChange` INTEGER, `imageQuality` REAL, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `WatchList` (`uid` TEXT NOT NULL, `name` TEXT, `type` TEXT, `note` TEXT, `createdOn` INTEGER, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `Subscriber` (`uid` TEXT NOT NULL, `name` TEXT, `alertVia` TEXT, `email` TEXT, `phone` TEXT, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `DistributionList` (`uid` TEXT NOT NULL, `name` TEXT, `created` INTEGER, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `SubscriberDistributionListCrossRef` (`uid` TEXT NOT NULL, `subscriber_id` TEXT, `distributionList_id` TEXT, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `AlertLog` (`uid` TEXT NOT NULL, `alertName` TEXT, `recognitionResult_id` TEXT, `smsSent` INTEGER, `emailSent` INTEGER, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `Alert` (`uid` TEXT NOT NULL, `name` TEXT, `watchlist_id` TEXT, `distributionList_id` TEXT, `location` TEXT, `created` INTEGER, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `EnrollmentReport` (`uid` TEXT NOT NULL, `date` INTEGER, `imageFile` TEXT, `status` TEXT, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `CrashLog` (`uid` TEXT NOT NULL, `title` TEXT, `content` TEXT, `resourcesStatus` TEXT, `date` INTEGER, `lastUpload` INTEGER, PRIMARY KEY(`uid`))");

                database.execSQL("CREATE TABLE IF NOT EXISTS `SubjectProfilePhoto` (`uid` TEXT NOT NULL, `profilePhoto` BLOB, PRIMARY KEY(`uid`))");


                //prepare variables(owner_device_uid)
                String device=android.os.Build.MANUFACTURER + android.os.Build.MODEL;
                String owner= App.currentUser.getUid();
                String preStr=owner+"_"+device+"_";

                //transfer data

                //subjects data
                database.execSQL("INSERT INTO Subject (uid, watchlist, firstName, lastName, ID, email, phone, address, features, imageQuality) " +
                        "SELECT '"+preStr+"'||num, watchlist, firstName, lastName, ID, email, phone, address, features, imageQuality FROM oldSubject");

                //WatchLists data
                database.execSQL("INSERT INTO WatchList (uid, name, type, note, createdOn) " +
                        "SELECT '"+preStr+"'||num, name, type, note, createdOn FROM oldWatchList");

//                RecognitionResultTable data
                database.execSQL("INSERT INTO RecognitionResult (uid, subjectId, date, features, bmp, live, scoreMatch, location, camera, lastChange, imageQuality) " +
                        "SELECT '"+preStr+"'||num, '"+preStr+"'||subjectId, date, features, bmp, live, scoreMatch, location, camera, lastChange, imageQuality FROM oldRecognitionResult");

                // Insert into Subscriber
                database.execSQL("INSERT INTO Subscriber (uid, name, alertVia, email, phone) " +
                        "SELECT '"+preStr+"'||num, name, alertVia, email, phone FROM oldSubscriber");

                // Insert into DistributionList
                database.execSQL("INSERT INTO DistributionList (uid, name, created) " +
                        "SELECT '"+preStr+"'||num, name, created FROM oldDistributionList");

//              Insert into SubscriberDistributionListCrossRef
                database.execSQL("INSERT INTO SubscriberDistributionListCrossRef (uid, subscriber_id, distributionList_id) " +
                        "SELECT '"+preStr+"'||num, '"+preStr+"'||subscriberId, '"+preStr+"'||distributionListId FROM oldSubscriberDistributionListCrossRef");

//              Insert into AlertLog
                database.execSQL("INSERT INTO AlertLog (uid, alertName, recognitionResult_id, smsSent, emailSent) " +
                        "SELECT '"+preStr+"'||num, alertName, '"+preStr+"'||recognitionResult, smsSent, emailSent FROM oldAlertLog");

//              Insert into Alert
                database.execSQL("INSERT INTO Alert (uid, name, watchlist_id, distributionList_id, location, created) " +
                        "SELECT '"+preStr+"'||num, name, '"+preStr+"'||watchlist, '"+preStr+"'||distributionList, location, created FROM oldAlert");

//              Insert into EnrollmentReport
                database.execSQL("INSERT INTO EnrollmentReport (uid, date, imageFile, status) " +
                        "SELECT '"+preStr+"'||num, date, image, status FROM oldEnrollmentReport");

//              Insert into CrashLog
                database.execSQL("INSERT INTO CrashLog (uid, title, content, resourcesStatus, date, lastUpload) " +
                        "SELECT '"+preStr+"'||num, title, content, resourcesStatus, date, lastUpload FROM oldCrashLog");



            }
        };
    }

    public static Migration dropOldTables() {
        return new Migration(21,22) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                // Drop the oldSubject table
                database.execSQL("DROP TABLE IF EXISTS oldSubject");

                // Drop the oldRecognitionResult table
                database.execSQL("DROP TABLE IF EXISTS oldRecognitionResult");

                // Drop the oldWatchList table
                database.execSQL("DROP TABLE IF EXISTS oldWatchList");

                // Drop the oldSubscriber table
                database.execSQL("DROP TABLE IF EXISTS oldSubscriber");

                // Drop the oldDistributionList table
                database.execSQL("DROP TABLE IF EXISTS oldDistributionList");

                // Drop the oldSubscriberDistributionListCrossRef table
                database.execSQL("DROP TABLE IF EXISTS oldSubscriberDistributionListCrossRef");

                // Drop the oldAlertLog table
                database.execSQL("DROP TABLE IF EXISTS oldAlertLog");

                // Drop the oldAlert table
                database.execSQL("DROP TABLE IF EXISTS oldAlert");

                // Drop the oldEnrollmentReport table
                database.execSQL("DROP TABLE IF EXISTS oldEnrollmentReport");

                // Drop the oldCrashLog table
                database.execSQL("DROP TABLE IF EXISTS oldCrashLog");

                // Drop the oldSubjectProfilePhoto table
                database.execSQL("DROP TABLE IF EXISTS oldSubjectProfilePhoto");

            }
        };
    }
}
