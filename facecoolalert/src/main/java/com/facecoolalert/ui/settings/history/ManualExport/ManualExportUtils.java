package com.facecoolalert.ui.settings.history.ManualExport;

import static com.facecoolalert.utils.AutoExportRecognitionHistory.AutoExportUtils.commonDate;
import static com.facecoolalert.utils.AutoExportRecognitionHistory.AutoExportUtils.showToast;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.utils.AutoExportRecognitionHistory.AutoExportThread;
import com.facecoolalert.utils.AutoExportRecognitionHistory.AutoExportUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ManualExportUtils {



    public static void exportHistory(Date start, Date end, Context context, ProgressBar progressBar, String saveFolder, Runnable onComplete, boolean deleteAfterExport) {
        progressBar.setProgress(0);
        List<String> toDelete=new ArrayList<>();
        File saveFile=new File(saveFolder,commonDate.format(start)+"-"+commonDate.format(end)+".zip");

        try{
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(saveFile));
            int pos = 0;
            Double exportedSize = 0.0;
            int fetchSize = 20;
            RecognitionResultDao recognitionResultDao= MyDatabase.getInstance(context).recognitionResultDao();
            SubjectDao subjectDao=MyDatabase.getInstance(context).subjectDao();
            int count=recognitionResultDao.getDateRangeRecognitionCount(start.getTime(), end.getTime());
            int progres=0;
            List<RecognitionResult> results = recognitionResultDao.getDateRangeRecognitionResults(start.getTime(), end.getTime(), fetchSize,pos);
            while(results.size()>0)
            {
                for(RecognitionResult history:results)
                {
                    ZipEntry recognitionEntry;
//                    recognitionEntry = new ZipEntry("Recognition_"+history.getUid()+"_"+commonDate.format(new Date(history.getDate()))+".png");
                    Subject subject=null;
                    if(history.getSubjectId()!=null)
                    {
                        try{
                            subject=subjectDao.getSubjectByuid(history.getSubjectId());
                        }catch (Exception es){}
                    }
                    if(subject!=null)
                        recognitionEntry = new ZipEntry("Recognition_"+history.getUid()+"_"+commonDate.format(new Date(history.getDate()))+"Subject_"+subject.getName()+"watchList"+subject.getWatchlist()+".png");
                    else
                        recognitionEntry = new ZipEntry("Recognition_"+history.getUid()+"_"+commonDate.format(new Date(history.getDate()))+".png");

                    zos.putNextEntry(recognitionEntry);
                    byte[] imageData=recognitionResultDao.getByteImage(history.getUid());
                    zos.write(imageData);


                    recognitionEntry=new ZipEntry("Features_"+history.getUid()+".bin");
                    zos.putNextEntry(recognitionEntry);
                    byte[] featuresData= history.getFeatures();
                    zos.write(featuresData);
                    zos.closeEntry();

                    if(deleteAfterExport)
                        toDelete.add(history.getUid());

                    progres++;
                    int percentage=(progres*100)/(count);
                    new Handler((Looper.getMainLooper())).post(()->{
                       progressBar.setProgress(percentage);
                    });

                }

                pos+=results.size();
                results = recognitionResultDao.getDateRangeRecognitionResults(start.getTime(), end.getTime(), fetchSize,pos);
            }

            byte[] infoFile= AutoExportUtils.generateInfoFile(start,pos,end);
            ZipEntry infoEntry=new ZipEntry("info.json");
            zos.putNextEntry(infoEntry);
            zos.write(infoFile);
            zos.closeEntry();

            zos.close();



            if(deleteAfterExport) {
                for (String todel : toDelete)
                    recognitionResultDao.updateByteImage(todel, null, null);
            }
            showToast(String.format("Exported Successfully ( %d items )",count),context);
            new Handler((Looper.getMainLooper())).post(onComplete);
        }catch (Exception es)
        {
            es.printStackTrace();
            showToast("An Error Occurred : "+es,context);
            new Handler((Looper.getMainLooper())).post(onComplete);
        }

    }
}
