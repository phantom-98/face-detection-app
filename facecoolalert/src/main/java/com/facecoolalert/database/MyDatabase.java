package com.facecoolalert.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.RawQuery;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.facecoolalert.App;
import com.facecoolalert.database.Repositories.AlertDao;
import com.facecoolalert.database.Repositories.AlertLogDao;
import com.facecoolalert.database.Repositories.CrashLogDao;
import com.facecoolalert.database.Repositories.DatabaseDao;
import com.facecoolalert.database.Repositories.DistributionListDao;
import com.facecoolalert.database.Repositories.EnrollmentReportDao;
import com.facecoolalert.database.Repositories.SubjectProfilePhotoDao;
import com.facecoolalert.database.Repositories.SubscriberDao;
import com.facecoolalert.database.Repositories.SubscriberDistributionListDao;
import com.facecoolalert.database.entities.Alert;
import com.facecoolalert.database.entities.AlertLog;
import com.facecoolalert.database.entities.CrashLog;
import com.facecoolalert.database.entities.DistributionList;
import com.facecoolalert.database.entities.EnrollmentReport;
import com.facecoolalert.database.entities.SubjectProfilePhoto;
import com.facecoolalert.database.entities.Subscriber;
import com.facecoolalert.database.entities.SubscriberDistributionListCrossRef;
import com.facecoolalert.database.entities.WatchList;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Callable;


@Database(entities = {Subject.class, RecognitionResult.class, WatchList.class, Subscriber.class, DistributionList.class,
        SubscriberDistributionListCrossRef.class, AlertLog.class,Alert.class, EnrollmentReport.class, CrashLog.class, SubjectProfilePhoto.class}, version = 22,
        exportSchema = true
) // Definition of entities and database version
public abstract class MyDatabase extends RoomDatabase {

    public static String DatabaseName="FaceCoolAlert";
    private static MyDatabase INSTANCE;

    public abstract SubjectDao subjectDao();

    public abstract RecognitionResultDao recognitionResultDao();


    public  abstract WatchlistDao watchlistDao();


    public abstract SubscriberDao subscriberDao();

    public abstract AlertLogDao alertLogDao();

    public abstract DistributionListDao distributionListDao();

    public abstract AlertDao alertDao();

    public abstract EnrollmentReportDao enrollmentReportDao();


    public abstract SubscriberDistributionListDao subscriberDistributionListDao();

    public abstract CrashLogDao crashLogDao();

    public abstract SubjectProfilePhotoDao subjectProfilePhotoDao();

    protected abstract DatabaseDao databaseDao();

    public static MyDatabase getInstance(Context context) {
        if (INSTANCE == null||!INSTANCE.isOpen()) {
            synchronized (MyDatabase.class) {
                if (INSTANCE == null||!INSTANCE.isOpen()) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MyDatabase.class,
                            DatabaseName
                    ).fallbackToDestructiveMigrationFrom(0,6)
                            .addMigrations(MigrationsList.migrations())
                            .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                            .build();


                }
            }
        }
        return INSTANCE;
    }

    public static void reLoadDatabase(Context context)
    {
        INSTANCE=null;
        getInstance(context);
    }


    public static MyDatabase getNewInstance(Context context, File dbFile) {
        if(dbFile.exists()) {
            MyDatabase tmp;
            try {
                tmp = Room.databaseBuilder(context.getApplicationContext(),
                                MyDatabase.class,
                                MyDatabase.DatabaseName)
                        .createFromFile(dbFile)
                        .fallbackToDestructiveMigrationFrom(0,6)
                        .addMigrations(MigrationsList.migrations())
                        .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                        .build();
                try{
                    if(INSTANCE!=null)
                        INSTANCE.close();

                }catch (Exception closingPrevConnection)
                {
                    closingPrevConnection.printStackTrace();
                }
//                return tmp;
                INSTANCE=tmp;
                return INSTANCE;
            }catch (Exception es)
            {
                es.printStackTrace();
                App.recordException(es);
            }

        }

        return null;
    }

    public static MyDatabase getNewInstance(Context context, Callable<InputStream> inputStream) throws Exception {
        if(inputStream!=null) {
            MyDatabase tmp;

                tmp = Room.databaseBuilder(context.getApplicationContext(),
                                MyDatabase.class,
                                MyDatabase.DatabaseName)
                        .createFromInputStream(inputStream)
                        .fallbackToDestructiveMigrationFrom(0,6)
                        .addMigrations(MigrationsList.migrations())
//                        .addCallback(callback)
                        .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                        .build();
                try{
                    if(INSTANCE!=null)
                        INSTANCE.close();
                }catch (Exception closingPrevConnection)
                {
                    closingPrevConnection.printStackTrace();
                }
//                return tmp;
                INSTANCE=tmp;
                return INSTANCE;
        }

        return null;
    }



    public void performCheckpoint() {
        try{
            INSTANCE.databaseDao().checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(truncate)"));
        }catch (Exception es)
        {
            es.printStackTrace();
        }
    }
}
