package com.facecoolalert.utils.AutoExportRecognitionHistory;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.ui.settings.history.HistoryFragment;
import com.facecoolalert.utils.GenUtils;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class AutoExportUtils {


    public static final String APP_IMPORT_HISTORY_NAME = "FaceCoolAlert";
    public static SimpleDateFormat commonDate = new SimpleDateFormat("ddMMMyyyy-HHmmss");


    public static void freeHistory(Double deleteSize, RecognitionResultDao recognitionResultDao,SubjectDao subjectDao) {

        try {
            File saveFile=new File(AutoExportThread.exportLocation, new SimpleDateFormat("YYYYMMMdddHHmmss").format(new Date())+"dummy.zip");
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(saveFile));
            int pos = 0;
            Double exportedSize = 0.0;
            int fetchSize = 1000;

            Long earliest=0L;
            Long lastest=0L;
            List<String> toDelete=new ArrayList<>();

            for (; exportedSize < deleteSize; ) {
                List<String> recognitions = recognitionResultDao.getEarlyRecognitionResults(fetchSize, pos);
                if(recognitions.size()==0)
                    break;

                for (String ri : recognitions) {
                    RecognitionResult history=recognitionResultDao.getByUid(ri);
                    if(history.getDate()<earliest||earliest==0L)
                        earliest=history.getDate();

                    ZipEntry recognitionEntry;
                    Subject subject=null;
                    if(history.getSubjectId()!=null)
                    {
                        try{
                            subject=subjectDao.getSubjectByuid(history.getSubjectId());
                        }catch (Exception es){}
                    }
                    if(subject!=null)
                        recognitionEntry = new ZipEntry("Recognition_"+ri+"_"+commonDate.format(new Date(history.getDate()))+"Subject_"+subject.getName()+"watchList"+subject.getWatchlist()+".png");
                    else
                        recognitionEntry = new ZipEntry("Recognition_"+ri+"_"+commonDate.format(new Date(history.getDate()))+".png");
                    zos.putNextEntry(recognitionEntry);
                    byte[] imageData=recognitionResultDao.getByteImage(ri);
                    zos.write(imageData);

                    recognitionEntry=new ZipEntry("Features_"+ri+".bin");
                    zos.putNextEntry(recognitionEntry);
                    byte[] featuresData= history.getFeatures();
                    zos.write(featuresData);
                    zos.closeEntry();

                    exportedSize+=(imageData.length+featuresData.length)/1024.0/1024.0/1024.0;
                    imageData=null;
                    featuresData=null;
                    pos++;

                    if(history.getDate()>lastest)
                        lastest=history.getDate();

                    toDelete.add(ri);

                    if(exportedSize>=deleteSize)
                        break;

                    try {
                        Thread.sleep(5);
                    }catch (Exception es)
                    {

                    }

                }

            }
            //create infoFile
            byte[] infoFile=generateInfoFile(earliest,pos,lastest);
            ZipEntry infoEntry=new ZipEntry("info.json");
            zos.putNextEntry(infoEntry);
            zos.write(infoFile);
            zos.closeEntry();

            zos.close();

            //rename File
            saveFile.renameTo(new File(AutoExportThread.exportLocation,commonDate.format(new Date(earliest))+"-"+commonDate.format(new Date(lastest))+".zip"));
            for(String todel:toDelete)
                recognitionResultDao.updateByteImage(todel,null,null);

        }catch (Exception es)
        {
            es.printStackTrace();
        }
    }

    private static byte[] generateInfoFile(Long earliest, int pos, Long latest) {
        JsonObject jo = new JsonObject();
        jo.addProperty("App", APP_IMPORT_HISTORY_NAME);
        jo.addProperty("Start From", commonDate.format(new Date(earliest)));
        jo.addProperty("Up To", commonDate.format(new Date(latest)));
        jo.addProperty("Number Of Entries", pos);
        jo.addProperty("exported On", commonDate.format(new Date()));

        return jo.toString().getBytes();
    }

    public static byte[] generateInfoFile(Date earliest, int pos, Date latest) {
        JsonObject jo = new JsonObject();
        jo.addProperty("App", APP_IMPORT_HISTORY_NAME);
        jo.addProperty("Start From", commonDate.format(earliest));
        jo.addProperty("Up To", commonDate.format(latest));
        jo.addProperty("Number Of Entries", pos);
        jo.addProperty("exported On", commonDate.format(new Date()));

        return jo.toString().getBytes();
    }

    public static void importHistory(Uri uri, Context context, HistoryFragment.ImportProgresChangeRunnable importProgresChangeRunnable) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ZipInputStream zinfo=new ZipInputStream(inputStream);

            //verify zip info
//            List<ZipEntry> entries=new ArrayList<>();
//            ZipEntry entry;
//            while ((entry = zin.getNextEntry()) != null) {
//                entries.add(entry);
//            }


            byte[] infoData=readZipEntry(zinfo,"info.json");
            JSONObject jo=new JSONObject(new String(infoData));
            if(!jo.getString("App").equals(APP_IMPORT_HISTORY_NAME)) {
//                Toast.makeText(context, "Couldn`t verify the History", Toast.LENGTH_SHORT).show();
                showToast("Couldn`t verify the History",context);
                return;
            }

            int entries=jo.getInt("Number Of Entries");
            Log.d("Import History","We got history "+entries);

            zinfo.close();
            inputStream = context.getContentResolver().openInputStream(uri);
            ZipInputStream zin = new ZipInputStream(inputStream);
            

            RecognitionResultDao recognitionResultDao= MyDatabase.getInstance(context).recognitionResultDao();
            int progres=0;
            //start traversing Recognitions
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String name=entry.getName();
                if(name.startsWith("Recognition"))
                {
                    byte[] imageData=readNextByte(zin);
                    String[] parts=name.split("_");
                    String uidString=parts[1];



                    if((entry = zin.getNextEntry()) != null)
                    {
                        if(entry.getName().equals("Features_"+uidString+".bin"))
                        {
                            byte[] featuresData=readNextByte(zin);

                            if(imageData!=null&&featuresData!=null) {
                                Log.d("Import History", "We made history " + uidString);
                                recognitionResultDao.restoreByteImage(uidString,imageData,featuresData,new Date().getTime());

                                progres++;
                                importProgresChangeRunnable.setProgres(progres * 100 / entries);
                                new Handler(Looper.getMainLooper()).post(importProgresChangeRunnable);
                            }
                        }
                    }
                    else {
//                        Toast.makeText(context, "Unable to locate features for recognition " + num, Toast.LENGTH_SHORT).show();
//                        return;
                        showToast("Unable to locate features for recognition " + uidString,context);
                    }


                }

                try {
                    Thread.sleep(5);
                }catch (Exception es)
                {

                }
            }




//            Toast.makeText(context, "Import Complete", Toast.LENGTH_SHORT).show();
            showToast("Import Complete",context);

        }catch (Exception es)
        {
            es.printStackTrace();
//            Toast.makeText(context, "Couldn`t find info file", Toast.LENGTH_SHORT).show();
            showToast("Couldn`t find info file",context);
        }
    }

    public static void showToast(String str,Context context)
    {
        new Handler(Looper.getMainLooper()).post(()->{
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        });
    }

    private static byte[] readNextByte(ZipInputStream zipInputStream) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zipInputStream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }


    public static byte[] readZipEntry(ZipInputStream zipInputStream, String entryName) throws IOException {
        try {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        byteArrayOutputStream.write(buffer, 0, len);
                    }
                    return byteArrayOutputStream.toByteArray();
                }
                zipInputStream.closeEntry();
            }
        }catch (Exception es) {
            throw new IllegalArgumentException("Entry not found: " + entryName);
        }
        return null;
    }


    public static void freeHistory(Double deleteSize, Context context) {
        RecognitionResultDao recognitionResultDao= MyDatabase.getInstance(context).recognitionResultDao();
        SubjectDao subjectDao=MyDatabase.getInstance(context).subjectDao();

        freeHistory(deleteSize,recognitionResultDao,subjectDao);
    }
}
