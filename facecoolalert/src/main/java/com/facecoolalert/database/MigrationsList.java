package com.facecoolalert.database;

import androidx.annotation.NonNull;
import androidx.room.RenameTable;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.facecoolalert.database.LongToStringMigration.LongToStringUidMigration;

public class MigrationsList {

    public static Migration[] migrations()
    {
        return new Migration[] {
                (new Migration(7,8) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {

                        database.execSQL("CREATE TABLE IF NOT EXISTS WatchList " +
                                "(num INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "name TEXT, " +
                                "type TEXT, " +
                                "note TEXT)");

                    }
                })
                ,(new Migration(8,9) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        try {
                            database.execSQL("ALTER TABLE WatchList ADD COLUMN createdOn INTEGER");
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                })
                ,(new Migration(9,10) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {

                        //create Alert table
                        database.execSQL("CREATE TABLE IF NOT EXISTS Alert (num INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, watchlist INTEGER, distributionList INTEGER, location TEXT)");

                        // Create the DistributionList table
                        database.execSQL("CREATE TABLE IF NOT EXISTS DistributionList (num INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");

                        // Create the Subscriber table
                        database.execSQL("CREATE TABLE IF NOT EXISTS Subscriber (num INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, alertVia TEXT, email TEXT, phone TEXT)");

                        // Create the SubscriberDistributionListCrossRef table
                        database.execSQL("CREATE TABLE IF NOT EXISTS SubscriberDistributionListCrossRef (num INTEGER PRIMARY KEY AUTOINCREMENT, subscriberId INTEGER, distributionListId INTEGER)");


                        database.execSQL("CREATE TABLE IF NOT EXISTS AlertLog " +
                                "(num INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "alertName TEXT, " +
                                "recognitionResult INTEGER, " +
                                "smsSent INTEGER, " +
                                "emailSent INTEGER)");
                    }
                })

                ,(new Migration(10,11) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("ALTER TABLE DistributionList ADD COLUMN created INTEGER");

                    }
                })

                ,(new Migration(11,14) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {

                        database.execSQL("alter table Alert add column created INTEger");

                    }
                })
                ,(new Migration(14,15) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {

                        database.execSQL(
                                "CREATE TABLE IF NOT EXISTS `EnrollmentReport` (`num` INTEGER PRIMARY KEY AUTOINCREMENT, `date` INTEGER, `image` TEXT, `status` TEXT)"
                        );

                    }
                })

                ,((new Migration(15,16) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {

                        database.execSQL(
                                "CREATE TABLE IF NOT EXISTS `CrashLog` (" +
                                        "`num` INTEGER PRIMARY KEY AUTOINCREMENT," +
                                        "`title` TEXT," +
                                        "`content` TEXT," +
                                        "`resourcesStatus` TEXT," +
                                        "`date` INTEGER)");
                    }
                }))

                ,(new Migration(16,17) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {

                        database.execSQL("ALTER TABLE RecognitionResult ADD COLUMN lastChange INTEGER DEFAULT 0");
                        database.execSQL("ALTER TABLE CrashLog ADD COLUMN lastUpload INTEGER DEFAULT 0");

                    }
                }),
                new Migration(17,18) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS `SubjectProfilePhoto` " +
                                "(`num` INTEGER NOT NULL, " +
                                "`profilePhoto` BLOB, " +
                                "PRIMARY KEY(`num`))");
                    }
                },
                new Migration(18,19) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("drop table User");//it was unused, it was used for early recognition tests.
                    }
                },
                new Migration(19,20) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("alter table Subject add column imageQuality Double default 0.0");
                        database.execSQL("alter table RecognitionResult add column imageQuality Double default 0.0");
                    }
                },
                LongToStringUidMigration.getMigration(),
                LongToStringUidMigration.dropOldTables(),
















        };
    }
}
