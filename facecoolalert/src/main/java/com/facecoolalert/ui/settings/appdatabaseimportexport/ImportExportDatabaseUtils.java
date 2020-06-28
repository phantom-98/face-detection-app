package com.facecoolalert.ui.settings.appdatabaseimportexport;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;

import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.Repositories.SubjectProfilePhotoDao;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.database.entities.SubjectProfilePhoto;

import java.util.List;
import java.util.Optional;

public class ImportExportDatabaseUtils {

    public static Boolean confirmCopyResult=false;


    public static void copyLocalPhotosToDb(Context context, ProgressBar activeBar) {
        MyDatabase db=MyDatabase.getInstance(context.getApplicationContext());
        SubjectProfilePhotoDao subjectProfilePhotoDao= db.subjectProfilePhotoDao();
        SubjectDao subjectDao=db.subjectDao();
//        RecognitionUtils.refreshSubjects();
        List<Subject> subjects=subjectDao.getAllSubjects();
        Log.d("Db Export"," Looking for Subjects count "+subjects.size());

        int progres=0;
        for(Subject subject: subjects)
        {
            Log.d("Db Export"," saving for "+subject.getName());
            Optional<SubjectProfilePhoto> optionalPhoto = subjectProfilePhotoDao.findBySubjectId(subject.getUid());
            do {
                try {
                    db.beginTransaction();
                    SubjectProfilePhoto photo;
                    Boolean itsEditing = false;
                    if (optionalPhoto.isPresent()) {
                        photo = optionalPhoto.get();
                        itsEditing = true;
                    } else
                        photo = new SubjectProfilePhoto(subject.getUid());
                    photo.loadFromLocal(context.getApplicationContext());
                    if (itsEditing)
                        subjectProfilePhotoDao.update(photo);
                    else
                        subjectProfilePhotoDao.insert(photo);

                    db.setTransactionSuccessful();
                    Log.d("Db Export"," saved profile photo for "+subject.getName());

                } catch (Exception es) {
                    es.printStackTrace();
                } finally {
                    // End the transaction
                    db.endTransaction();
                    optionalPhoto = subjectProfilePhotoDao.findBySubjectId(subject.getUid());
                    if(confirmCopyResult&&!optionalPhoto.isPresent()) {
                        try {
                            Thread.sleep(50);//wait if changes will have reflected after 50 milliseconds
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        optionalPhoto = subjectProfilePhotoDao.findBySubjectId(subject.getUid());//give it a second try before the whole processing of saving the photo is repeated
                    }
                }
            }while(confirmCopyResult&&!optionalPhoto.isPresent()&&subject.newImageSaveFile(context).exists());
            try {
                progres++;
                int finalProgres = (int) (60 * progres /subjects.size());
                Log.d("Export Db","Progress : "+finalProgres);
                new Handler(Looper.getMainLooper()).post(() -> {
                    activeBar.setProgress(finalProgres, true);
                });
            }catch (Exception es)
            {
                es.printStackTrace();
            }
        }
    }

    public static void deletePhotosFromImportDb(MyDatabase importDb, Context context) {
        SubjectProfilePhotoDao subjectProfilePhotoDao= importDb.subjectProfilePhotoDao();
        subjectProfilePhotoDao.deleteAll();
    }
}
