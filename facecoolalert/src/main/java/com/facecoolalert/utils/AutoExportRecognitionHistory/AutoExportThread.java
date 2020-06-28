package com.facecoolalert.utils.AutoExportRecognitionHistory;

import android.content.Context;

import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.utils.PrefManager;

public class AutoExportThread extends Thread{

    private RecognitionResultDao recognitionResultDao;

    private Context context;

    public static Double maxSize;

    public static Double deleteSize;

    private Boolean killed=false;

    public static String exportLocation;


    public AutoExportThread(Context context)
    {
        this.context=context;
//        this.recognitionResultDao= MyDatabase.getInstance(context).recognitionResultDao();
        maxSize= (double) PrefManager.getMaxHistorySize(context);
        deleteSize= (double) PrefManager.getHistoryDeleteSize(context);
        exportLocation=PrefManager.getHistoryExportFolder(context);
    }


    public Double getHistorySize()
    {
        Double size=0.0;
        try {
           size = recognitionResultDao.totalBitmapsLength();
           if(size==null)
               size=0.0;
        }catch (Exception es)
        {
//            es.printStackTrace();
        }

        return size;
    }





    @Override
    public void run()
    {
        while(context!=null&&!killed)
        {
            try {
                if (getHistorySize() >= maxSize)
                    AutoExportUtils.freeHistory(deleteSize, context);

            }catch (Exception es)
            {
//                es.printStackTrace();
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void killAll() {
        killed=true;
    }
}
