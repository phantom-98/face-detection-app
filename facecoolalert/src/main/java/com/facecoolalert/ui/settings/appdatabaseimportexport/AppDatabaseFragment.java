package com.facecoolalert.ui.settings.appdatabaseimportexport;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facecoolalert.App;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.Repositories.SubjectProfilePhotoDao;
import com.facecoolalert.databinding.FragmentAppDatabaseImportexportBinding;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.database.entities.SubjectProfilePhoto;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.utils.RecognitionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;


public class AppDatabaseFragment extends Fragment {

    private static AppDatabaseFragment previous;

    private static final String APP_NAME = "FaceCoolAlert";
    private static final String APP_IMPORT_TYPE = "FaceCoolAlertDatabase";

    private static final int REQUEST_CODE_PICK_DIRECTORY = 141;
    private static String DEFAULT_FILE_NAME = "FaceCoolAlertDatabase.db";
    private static int REQUEST_CODE_PICK_FILE = 107;

    private FragmentAppDatabaseImportexportBinding binding;

    private SubjectDao subjectDao;

    private List<Subject> subjectList;

    private ProgressBar activeBar=null;


    public static AppDatabaseFragment getInstance()
    {
        if(previous==null)
            previous=new AppDatabaseFragment();

        return previous;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        binding=FragmentAppDatabaseImportexportBinding.inflate(inflater,container,false);

        feedView();

        subjectDao=MyDatabase.getInstance(getContext()).subjectDao();
        new Thread(()->{
            try {
                subjectList = subjectDao.getAllSubjects();
            }catch (Exception es)
            {
                es.printStackTrace();
            }
        }).start();

        return binding.getRoot();
    }

    private void feedView() {

        binding.backButton.setOnClickListener((v)->{
            ((MainActivity)getActivity()).removeFragment(AppDatabaseFragment.class);
        });

        binding.importappDatabase.setOnClickListener(v->{
            openFilePicker();
        });

        binding.exportappDatabase.setOnClickListener(v->{
            SimpleDateFormat dateF=new SimpleDateFormat("ddMMMyyyyHHmmss");
            fileSavePicker(APP_IMPORT_TYPE+dateF.format(new Date())+".db");
        });

        if(activeBar!=null) {
            activeBar=binding.exportProgressBar;
            activeBar.setVisibility(View.VISIBLE);
        }

        checktodisableActions();
        MyDatabase.reLoadDatabase(getContext());

    }

    private void checktodisableActions() {
        new Handler(Looper.getMainLooper()).post(()->{
        if(activeBar!=null)
        {
            activeBar.setVisibility(View.VISIBLE);
            binding.exportappDatabase.setEnabled(false);
            binding.importappDatabase.setEnabled(false);
        }
        else{
            binding.exportappDatabase.setEnabled(true);
            binding.importappDatabase.setEnabled(true);
        }
        });
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }


    private void fileSavePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.sqlite3");
        intent.putExtra(Intent.EXTRA_TITLE, DEFAULT_FILE_NAME);
        startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY);
    }
    private void fileSavePicker(String preferredFilename) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, preferredFilename);
        startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_DIRECTORY && resultCode == RESULT_OK && data != null) {
            // Get the URI of the selected file
            Uri uri = data.getData();
            new Thread(()->{
                try {
                    exportData(uri,1);
                }catch (Exception es)
                {
                    es.printStackTrace();
                }
            }).start();

        } else if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            // Get the URI of the selected JSON file
            Uri uri = data.getData();
            if(uri.getLastPathSegment().endsWith(".db")||true)//allow all
            {
            new Thread(()->{
                importData(uri,1);
            }).start();
            }
            else{
                Log.d("Import Database ",uri.getLastPathSegment());
                Toast.makeText(getContext(),"The database file needs to have the extension .db ",Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void exportData(Uri uri,int trial) {

        try{
            activeBar=binding.exportProgressBar;
//            activeBar.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).post(()->{if(activeBar!=null){activeBar.setVisibility(View.VISIBLE);}});
            checktodisableActions();
            if(activeBar!=null)
                new Handler(Looper.getMainLooper()).post(()->{if(activeBar!=null){activeBar.setProgress(0,true);}});
            OutputStream outputStream=getContext().getContentResolver().openOutputStream(uri);
            ImportExportDatabaseUtils.copyLocalPhotosToDb(getContext(),activeBar);

            if(activeBar!=null)
                new Handler(Looper.getMainLooper()).post(()->{if(activeBar!=null){activeBar.setProgress(60,true);}});
            MyDatabase.getInstance(getContext()).performCheckpoint();
            MyDatabase.getInstance(getContext()).close();
            File dbFile=getContext().getDatabasePath(MyDatabase.DatabaseName);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android version is Q or later
                FileUtils.copy(new FileInputStream(dbFile), outputStream,
                        null
                        , null
                        , new FileUtils.ProgressListener() {
                            @Override
                            public void onProgress(long progress) {
                                // Handle progress updates if needed
                                if(activeBar!=null)
                                    new Handler(Looper.getMainLooper()).post(()->{activeBar.setProgress((int) (60+0.4*progress),true);});
                               Log.d("Export Db","progres : "+progress);

                            }
                        }
                );
            } else {
                // Android version is below Q
//                FileU.copy(new FileInputStream(dbFile), outputStream);
                Files.copy(dbFile.toPath(),outputStream);
                Log.d("Export Db","progres2 : "+100);
            }
            if(activeBar!=null)
                new Handler(Looper.getMainLooper()).post(()->{if(activeBar!=null){activeBar.setProgress( (100),true);}});

            new Handler(Looper.getMainLooper()).post(()->{Toast.makeText(getContext(), "Exported Successfully", Toast.LENGTH_SHORT).show();});

            Log.d("Export Db","alerted Complete ");


            MyDatabase.reLoadDatabase(getContext());
        }catch (Exception es)
        {
            MyDatabase.reLoadDatabase(getContext());
            if(trial<=3)
                exportData(uri,trial+1);
            else {
                es.printStackTrace();
                App.saveException(es,getContext());
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), "Error exporting : " + es, Toast.LENGTH_SHORT).show();
                });
            }
        }

        new Handler(Looper.getMainLooper()).post(()->{if(activeBar!=null){activeBar.setVisibility(View.GONE);}});
        try {
            Thread.sleep(5000);
            activeBar=null;
            Log.d("Export Db","set to null");
            checktodisableActions();
            ImportExportDatabaseUtils.deletePhotosFromImportDb(MyDatabase.getInstance(getContext()),getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }





//    private void exportData(Uri uri) {
//        try {
//            activeBar=binding.exportProgressBar;
//            checktodisableActions();
//
//            OutputStream outputStream=getContext().getContentResolver().openOutputStream(uri);
//            ZipOutputStream zos=new ZipOutputStream(outputStream);
//
//            // Set the compression level to BEST_SPEED
//            zos.setLevel(Deflater.BEST_SPEED);
//
//            ZipEntry entry=new ZipEntry("info.json");
//            zos.putNextEntry(entry);
//            zos.write(createInfoEntry().getBytes());
//            zos.closeEntry();
//
//            new Handler(Looper.getMainLooper()).post(()->{
//                activeBar.setVisibility(View.VISIBLE);
//                activeBar.setProgress(0,true);
//            });
//
//
//            ZipEntry dbFileEntry=new ZipEntry(MyDatabase.DatabaseName+".db");
//
//            zos.putNextEntry(dbFileEntry);
//            File dbFile=getContext().getDatabasePath(MyDatabase.DatabaseName);
//            InputStream inputStream = new FileInputStream(dbFile);
//            dbFileEntry.setSize(dbFile.length());
//
//            Long totalREad=0L;
//            {
//                byte[] buffer = new byte[1024*8];
//                int bytesRead;
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    zos.write(buffer, 0, bytesRead);
//                    totalREad+=bytesRead;
//
//                    long finalTotalREad = totalREad;
//                    new Handler(Looper.getMainLooper()).post(()->{
//                        Long progres=(finalTotalREad*50) /dbFile.length();
//                        //Log.d("ExportDatabase","exporting database, progres : "+progres +" from "+finalTotalREad+"/"+dbFile.length());
//                        activeBar.setProgress(Math.toIntExact(progres),true);
//                    });
//                }
//            }
//            zos.closeEntry();
//
//            int subjectsProgres=0;
//            //write subject images
//            for(Subject subject:subjectList)
//            {
//                ZipEntry subjectProfilezipEntry=new ZipEntry("subjectImages/Subjectimage"+subject.getNum()+".png");
//                zos.putNextEntry(subjectProfilezipEntry);
//                zos.write(subject.loadImageBytes(getContext()));
//                zos.closeEntry();
//                subjectsProgres++;
//                int finalSubjectsProgres = subjectsProgres;
//                new Handler(Looper.getMainLooper()).post(()->{
//                    activeBar.setProgress(50+ finalSubjectsProgres /subjectList.size()*50,true);
//                });
//            }
//
//
//            zos.close();
//            outputStream.close();
//
//
//            new Handler(Looper.getMainLooper()).post(()->{
//                try {
//                    activeBar.setVisibility(View.INVISIBLE);
//                    Toast.makeText(activeBar.getContext(), "Exported Successfully", Toast.LENGTH_SHORT).show();
//                }catch (Exception es)
//                {
//
//                }
//                activeBar=null;
//                checktodisableActions();
//            });
////            activeBar=null;
//        } catch (Exception e) {
//            e.printStackTrace();
//            new Handler(Looper.getMainLooper()).post(()->{
//                Toast.makeText(getContext(),"Error exporting : "+e,Toast.LENGTH_SHORT).show();
//            });
//        }
//
//    }

    private void importData(Uri uri,int trial) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);

            File dbFile = getContext().getDatabasePath(MyDatabase.DatabaseName);

            File dbFileshm = getContext().getDatabasePath(MyDatabase.DatabaseName+"-shm");
            File dbFilewal = getContext().getDatabasePath(MyDatabase.DatabaseName+"-wal");
            File dbFilemark = getContext().getDatabasePath(MyDatabase.DatabaseName+".mark");

            activeBar = binding.importProgressBar;
            checktodisableActions();
            new Handler(Looper.getMainLooper()).post(() -> {
                activeBar.setVisibility(View.VISIBLE);
                activeBar.setProgress(0, true);
            });

            Callable<InputStream> inputStreamCallable = () -> inputStream;


            MyDatabase.getInstance(getContext()).close();
            if(dbFile.exists())
                dbFile.delete();
            if(dbFileshm.exists())
                dbFileshm.delete();
            if(dbFilewal.exists())
                dbFilewal.delete();
            if(dbFilemark.exists())
                dbFilemark.delete();

            MyDatabase importDb = MyDatabase.getNewInstance(getContext(), inputStreamCallable);
            if (importDb != null) {
                try {//delete existing profile images
                    for (Subject subject : subjectDao.getAllSubjects())
                        subject.deleteImage(getContext());
                }catch (Exception es)
                {

                }
                importDb.subjectDao().listSubjects();
                RecognitionUtils.refreshSubjects();
                new Handler(Looper.getMainLooper()).post(()->{if(activeBar!=null){activeBar.setProgress( (80),true);}});
                SubjectProfilePhotoDao subjectProfilePhotoDao=importDb.subjectProfilePhotoDao();

                try {
                    int total_photos=0;
                    int offset=0;
                    int fetchSize=100;
                    List<SubjectProfilePhoto> profilePhotosList = subjectProfilePhotoDao.fetchRange(offset,fetchSize);
                    while(!profilePhotosList.isEmpty())
                    {
                        for (SubjectProfilePhoto photo :profilePhotosList) {
                            try {
                                photo.saveToLocal(getContext());
                            } catch (Exception es) {
                                es.printStackTrace();
                            }
                        }
                        int currSize=profilePhotosList.size();
                        total_photos+=currSize;
                        offset=total_photos;
                        profilePhotosList = subjectProfilePhotoDao.fetchRange(offset,fetchSize);
                        Log.d("Import Database", String.format("import Subject Images : %d  Total Cummulative : %d",currSize, total_photos));
                    }

                }catch (Exception esm)
                {
                    esm.printStackTrace();
                }

                new Handler(Looper.getMainLooper()).post(()->{if(activeBar!=null){activeBar.setProgress( (90),true);}});
                ImportExportDatabaseUtils.deletePhotosFromImportDb(importDb,getContext());

                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        Toast.makeText(getContext(), "Imported Successfully  ", Toast.LENGTH_SHORT).show();
                        activeBar.setVisibility(View.INVISIBLE);
                        activeBar = null;
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                    }
                    checktodisableActions();
                });
            }


        } catch (Exception es) {
//            throw new RuntimeException(e);

            es.printStackTrace();
            if(trial<=3)
                importData(uri,trial+1);
            else {
                App.saveException(es,getContext());
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), "Error importing: " + es, Toast.LENGTH_SHORT).show();
                    activeBar.setVisibility(View.INVISIBLE);
                    activeBar = null;
                    checktodisableActions();
                });
            }
        }
    }


//
//    private void importData(Uri uri) {
//
//        try {
//            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
//
//            ZipInputStream zin=new ZipInputStream(inputStream);
//            File dbFile=getContext().getDatabasePath(MyDatabase.DatabaseName);
//
//            ZipEntry infoEntry=zin.getNextEntry();
//            if(!infoEntry.getName().equals("info.json")) {
//                new Handler(Looper.getMainLooper()).post(()->{
//                Toast.makeText(getContext(), "File has no info.json ", Toast.LENGTH_SHORT).show();
//                });
//                return;
//            }
//
//            JSONObject infos=new JSONObject(new String(FileUtils.readAllBytes(zin)));
//            System.out.println(infos.toString());
//            zin.closeEntry();
//            if(infos.getString("AppName").equals(APP_NAME)&&infos.getString("ImportType").equals(APP_IMPORT_TYPE))
//            {}else{
//                new Handler(Looper.getMainLooper()).post(()-> {
//                    Toast.makeText(getContext(), "File is not a " + APP_NAME + " Database Import File", Toast.LENGTH_SHORT).show();
//                });
//                return;
//            }
//
//
//            ZipEntry dbEntry=zin.getNextEntry();
//            if(dbEntry.getName().equals(MyDatabase.DatabaseName+".db"))
//            {
//
//                activeBar=binding.importProgressBar;
//                checktodisableActions();
//                new Handler(Looper.getMainLooper()).post(()->{
//                    activeBar.setVisibility(View.VISIBLE);
//                    activeBar.setProgress(0,true);
//                });
//
//            }else {
//                new Handler(Looper.getMainLooper()).post(()-> {
//                    Toast.makeText(getContext(), "File does not contain a " + APP_NAME + " Database File", Toast.LENGTH_SHORT).show();
//                });
//                return;
//            }
//            MyDatabase.reLoadDatabase(getContext());
//
//
//            File TemporaryFile=new File(dbFile.getParentFile(),"tempDatabase.db");
//
//            //code to load database file
//            if(true)
//            {
//
//                OutputStream dbOutputStream = new FileOutputStream(TemporaryFile);
//                Long totalREad = 0L;
//                final Long[] totalSize = {dbEntry.getSize()};
//                {
//                    byte[] buffer = new byte[1024 * 8];
//                    int bytesRead;
//                    while ((bytesRead = zin.read(buffer)) != -1) {
//                        dbOutputStream.write(buffer, 0, bytesRead);
//                        totalREad += bytesRead;
//
//                        long finalTotalREad = totalREad;
//                        new Handler(Looper.getMainLooper()).post(() -> {
//                            try {
//
//                                if (totalSize[0] > 0) {
//                                } else {
//                                    totalSize[0] = Long.valueOf(zin.available());
//                                }
//
//                                Long progres = (finalTotalREad * 50) / totalSize[0];
//                                if (progres > 50 || progres < 0)
//                                    progres = 50L;
//
//                                Log.d("Import Database", "importing database, progres : " + progres + " from " + finalTotalREad + "/" + totalSize[0]);
//                                activeBar.setProgress(Math.toIntExact(progres), true);
//                            } catch (Exception es) {
////                            es.printStackTrace();
//                            }
//                        });
//                    }
//                }
//            }
//            zin.closeEntry();
//
////            Room.databaseBuilder(getContext().getApplicationContext(), MyDatabase.class, MyDatabase.DatabaseName)
////                    .createFromFile(TemporaryFile)
////                    .build();
//            MyDatabase.getInstance(getContext()).close();
//            try{
//                dbFile.delete();
//
//                for(Subject i:subjectList)
//                    i.deleteImage(getContext());
//
//            }catch (Exception es)
//            {
//                es.printStackTrace();
//            }
//
////            MyDatabase.getNewInstance(getContext(),TemporaryFile);
//
//            MyDatabase dbConnection=MyDatabase.getNewInstance(getContext(),TemporaryFile);
//            WatchlistDao watchlistDao=dbConnection.watchlistDao();
//            System.out.println("Watch lists "+watchlistDao.listAll());
//
//            try {
//                Thread.sleep(5000);
//                TemporaryFile.delete();
//            }catch (Exception es)
//            {
//                es.printStackTrace();
//            }
//            RecognitionUtils.refreshSubjects();
//
//            int numSubjects=infos.getInt("No of Subjects");
//            int subjectProgres=0;
//            ZipEntry entry;
//            while ((entry = zin.getNextEntry()) != null) {
//                String name = entry.getName();
//                System.out.println("name is "+name);
//                if (name.startsWith("subjectImages/Subjectimage")) {
//                    try {
//                        String subjectnum = name.replaceAll("[a-zA-Z./]", "");
//                        System.out.println("name is " + subjectnum);
//                        Long num = Long.valueOf(Integer.valueOf(subjectnum));
//                        Subject tmpSubject = new Subject();
//                        tmpSubject.setNum(num);
//                        tmpSubject.saveImageBytes(getContext(), FileUtils.readAllBytes(zin));
//                        zin.closeEntry();
//                        subjectProgres++;
//                        int finalSubjectsProgres = subjectProgres;
//                        new Handler(Looper.getMainLooper()).post(() -> {
//                            activeBar.setProgress(50 + finalSubjectsProgres / numSubjects, true);
//                        });
//                    }catch (Exception es)
//                    {
//                        es.printStackTrace();
//                    }
//                }
//
//            }
//
//
//
//            activeBar=binding.importProgressBar;
//            checktodisableActions();
//            new Handler(Looper.getMainLooper()).post(()->{
//                activeBar.setProgress(0);
//            });
//            MyDatabase.reLoadDatabase(getContext());
//
//            new Handler(Looper.getMainLooper()).post(()->{
//                try {
//                    activeBar.setVisibility(View.INVISIBLE);
//                    Toast.makeText(activeBar.getContext(), "Imported Successfully", Toast.LENGTH_SHORT).show();
//
////                    new AlertDialog.Builder(getContext()).
////                            setMessage("The Database has been imported Successfully. Lets Restart App to be able to Write Data.")
////                            .setNegativeButton("Not Now", (dialog, i)->{
////
////
////                            })
////                            .setPositiveButton("Restart App", ((dialogInterface, i) -> {
////                                restartApp(getContext().getApplicationContext());
////                            })).create().show();
//
//
//                }catch (Exception es)
//                {
//
//                }
//                activeBar=null;
//                checktodisableActions();
//            });
//
//        }catch (Exception es)
//        {
//            es.printStackTrace();
//            new Handler(Looper.getMainLooper()).post(()->{
//                Toast.makeText(getContext(),"Error importing: "+es,Toast.LENGTH_SHORT).show();
//                activeBar.setVisibility(View.INVISIBLE);
//                activeBar=null;
//                checktodisableActions();
//            });
//        }
//
//    }
//
//    private String createInfoEntry() throws Exception{
//        JSONObject jo=new JSONObject();
//        jo.put("AppName",APP_NAME);
//        jo.put("ImportType",APP_IMPORT_TYPE);
//        jo.put("No of Subjects", subjectList.size());
//        jo.put("ExportDate",new Date());
//
//       return jo.toString();
//    }
//
//    public static void restartApp(Context context) {
//        // Close the Room database
//        MyDatabase.getInstance(context).close();
//
//        // Restart the app by restarting the process
//        Intent restartIntent = context.getPackageManager()
//                .getLaunchIntentForPackage(context.getPackageName());
//        if (restartIntent != null) {
//            restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    | Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            // Check if there are any other running processes related to the app and stop them
//            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//            if (activityManager != null) {
//                List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
//                for (ActivityManager.AppTask appTask : appTasks) {
//                    appTask.finishAndRemoveTask();
//                }
//            }
//
//            context.startActivity(restartIntent);
//
//            // Finish the current activity
//            if (context instanceof Activity) {
//                ((Activity) context).finish();
//            }
//        }
//    }


}
