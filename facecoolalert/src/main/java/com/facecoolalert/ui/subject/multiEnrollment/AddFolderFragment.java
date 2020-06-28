package com.facecoolalert.ui.subject.multiEnrollment;

import static android.app.Activity.RESULT_OK;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.facecool.attendance.facedetector.FaceData;
import com.facecool.cameramanager.utils.BitmapUtils;
import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.EnrollmentReportDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.EnrollmentReport;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.ui.detect.FaceDetectorProcessor;
import com.facecoolalert.ui.subject.multiEnrollment.FolderPicker.AddFolderViewModel;
import com.facecoolalert.utils.GenUtils;
import com.facecoolalert.utils.RecognitionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddFolderFragment extends Fragment {

    private String folderPath;
    private AddFolderViewModel mViewModel;
    private RecyclerView recyclerView;
    private static int progres;
    private static int tot;
    private static int enrolled;
    private static int error;

    private Long enrollmentDate;

    private EnrollmentReportDao enrollmentReportDao;

    private TextView tv_info_treashold;

    private static String prevFolderPath="/";

    public AddFolderFragment(String folderPath)
    {
        this.folderPath=folderPath;
        prevFolderPath=folderPath;
        this.enrollmentDate=new Date().getTime();
        this.enrollmentReportDao=MyDatabase.getInstance(getContext()).enrollmentReportDao();
        this.progres=0;
        this.enrolled=0;
        this.error=0;
    }

    public static AddFolderFragment newInstance(String folderPath) {
        return new AddFolderFragment(folderPath);
    }

    public static AddFolderFragment newInstance() {
        return new AddFolderFragment(prevFolderPath);
    }

    public AddFolderFragment()
    {
        this(prevFolderPath);
    }

    public AddFolderFragment(Bundle args)
    {
        setArguments(args);
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View myView=inflater.inflate(R.layout.dialog_add_folder_alert, container, false);

        myView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent resultIntent = new Intent();
                resultIntent.putExtra("subjectcard", "updated");
                getActivity().setResult(RESULT_OK, resultIntent);
                getActivity().finish();
            }
        });

        this.recyclerView=myView.findViewById(R.id.rv_enroll_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        FolderPhotosAdapter folderPhotosAdapter=new FolderPhotosAdapter();
        recyclerView.setAdapter(folderPhotosAdapter);
        return myView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddFolderViewModel.class);
        mViewModel.setFolderPath(folderPath);
        mViewModel.getItemListLiveData().observe(getViewLifecycleOwner(), items -> {
            // Update the adapter with the new data when it changes
            FolderPhotosAdapter folderPhotosAdapter=(FolderPhotosAdapter)recyclerView.getAdapter();
            folderPhotosAdapter.setItems(items);
            tot=items.size();
            final TextView infotext=recyclerView.getRootView().findViewById(R.id.info_text);
            updateStats(infotext);
            Button start=recyclerView.getRootView().findViewById(R.id.btn_start);
            Button pause=recyclerView.getRootView().findViewById(R.id.btn_pause);
            start.setOnClickListener(v -> {
                v.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                new Thread(()->processImages(items,v)).start();
            });

            pause.setOnClickListener(v -> {
                if(paused)
                {
                    paused=false;
                    pause.setText("Pause");
                    new Thread(()-> processImages(items,v)).start();
                }
                else{
                    paused=true;
                    pause.setText("Resume");
                }
            });
        });

        tv_info_treashold= recyclerView.getRootView().findViewById(R.id.tv_info_treashold);
        tv_info_treashold.setText(String.format("Quality Threshold : %.0f %%",RecognitionUtils.enrollmentQualityThreshold));

    }

    private int pos=0;
    private Boolean paused=false;

    private void processImages(List<File> items,View v) {
        FolderPhotosAdapter folderPhotosAdapter=(FolderPhotosAdapter)recyclerView.getAdapter();
        final CheckBox scroll=recyclerView.getRootView().findViewById(R.id.check_follow);
        final TextView infotext=recyclerView.getRootView().findViewById(R.id.info_text);
        Handler handler=new Handler(Looper.getMainLooper());
        for(int i = pos; i<tot&&!((Activity)v.getContext()).isFinishing()&&!paused; i++) {
            int finalI = i;
            folderPhotosAdapter.holderstatus.put(i, "processing");
            handler.post(() -> {
                folderPhotosAdapter.notifyItemChanged(finalI);
                if(scroll.isChecked())
                    recyclerView.smoothScrollToPosition(finalI);
            });
            final Boolean[] completed = {false};
            final Boolean[] exited={false};

            Boolean alwaysNotify=true;

            //start Detecting
            ExecutorService threadPool= Executors.newCachedThreadPool();
            FaceDetectorProcessor processor = new FaceDetectorProcessor(v.getContext(), new FaceDetectorProcessor.ResultListener() {
                @Override
                public void onSuccess(FaceData faceData, boolean bOnlyReg) {
                    threadPool.submit(()->{
                    progres++;
                    completed[0]=true;

                        if(faceData.getFeatures()==null) {
                            error++;
                            completed[0] =true;
                            //placeError(currentHolder,"No Face Detected");
                            folderPhotosAdapter.holderstatus.put(finalI, "Unable to extract features from the image");
                            new Thread(()->{
                                EnrollmentReport enrollmentReport=new EnrollmentReport();
                                enrollmentReport.setDate(enrollmentDate);
                                enrollmentReport.setStatus(folderPhotosAdapter.holderstatus.get(finalI));
                                enrollmentReport.setImageFile(items.get(finalI).toString());
                                enrollmentReportDao.insert(enrollmentReport);
                            }).start();

                            if(!exited[0]||alwaysNotify) {
                                handler.post(() -> {
                                    folderPhotosAdapter.notifyItemChanged(finalI);
                                    updateStats(infotext);
                                });
                            }
                        }
                        else {

                            folderPhotosAdapter.fetchedBitmaps.put(finalI, faceData.getBestImage());
                            if (faceData.getScoreMatch() >= RecognitionUtils.similarityThreshold || faceData.getImageQualityScore()<RecognitionUtils.enrollmentQualityThreshold) {
                                error++;
                                //placeError(currentHolder,"Face Already Enrolled");
                                if(faceData.getScoreMatch() >= RecognitionUtils.similarityThreshold)
                                    folderPhotosAdapter.holderstatus.put(finalI, "Face Already Enrolled as ("+faceData.getName()+") "+ GenUtils.roundScore(faceData.getScoreMatch())+"%");
                                else
                                    folderPhotosAdapter.holderstatus.put(finalI, "Quality Below Threshold "+ GenUtils.roundScore(faceData.getImageQualityScore())+"%");

                                new Thread(()->{
                                    EnrollmentReport enrollmentReport=new EnrollmentReport();
                                    enrollmentReport.setDate(enrollmentDate);
                                    enrollmentReport.setStatus(folderPhotosAdapter.holderstatus.get(finalI));
                                    enrollmentReport.setImageFile(items.get(finalI).toString());
                                    enrollmentReportDao.insert(enrollmentReport);
                                }).start();

                                if (!exited[0]||alwaysNotify) {
                                    handler.post(() -> {
                                        folderPhotosAdapter.notifyItemChanged(finalI);
                                        updateStats(infotext);
                                    });
                                }

                            } else {
                                enrolled++;
                                String fileName = items.get(finalI).getName();
                                String[] nameAndExtension = fileName.split("\\.");
                                String filename = nameAndExtension[0];
                                SubjectDao subjectDao = MyDatabase.getInstance(getContext()).subjectDao();
                                Subject mysubject = new Subject();
                                if (fileName.contains("_")) {
                                    String[] nameParts = filename.split("_");
                                    mysubject.setFirstName(nameParts[0]);
                                    mysubject.setLastName(nameParts[1]);
                                } else {
                                    mysubject.setFirstName(filename);
                                    mysubject.setLastName(filename);
                                }
                                mysubject.setFeatures(faceData.getFeatures());
                                mysubject.setImageQuality(faceData.getImageQualityScore());
                                mysubject.setWatchlist("Default");
                                try {
                                    subjectDao.insertSubject(mysubject);
                                    if(mysubject.getFeatures()==null||faceData.getBestImage()==null) {
                                        folderPhotosAdapter.holderstatus.put(finalI, "Unable to get Face Features");
                                        enrolled--;
                                    }
                                    else {
                                        mysubject.saveImage(getContext(), faceData.getBestImage());
                                        folderPhotosAdapter.holderstatus.put(finalI, "success ("+ GenUtils.roundScore(faceData.getImageQualityScore())+" %)");
                                        new Thread(() -> {
                                            EnrollmentReport enrollmentReport = new EnrollmentReport();
                                            enrollmentReport.setDate(enrollmentDate);
                                            enrollmentReport.setStatus(folderPhotosAdapter.holderstatus.get(finalI));
                                            enrollmentReport.setImageFile(items.get(finalI).toString());
                                            enrollmentReportDao.insert(enrollmentReport);
                                        }).start();
                                        RecognitionUtils.subjects.add(mysubject);
                                    }
                                } catch (Exception eds) {
                                    eds.printStackTrace();
                                    Toast.makeText(getContext(), "Error saving : " + eds, Toast.LENGTH_SHORT).show();
                                    folderPhotosAdapter.holderstatus.put(finalI, "Unable to get Face Features");
                                }
                                if (!exited[0]||alwaysNotify) {
                                    handler.post(() -> {
                                        folderPhotosAdapter.notifyItemChanged(finalI);
                                        updateStats(infotext);
                                    });
                                }
                            }
                        }
                    });
                }

                @Override
                public void onSuccessNull() {
                    threadPool.submit(()->{
                    progres++;
                    error++;
                    completed[0] =true;
                    folderPhotosAdapter.holderstatus.put(finalI, "No Face Detected");
                        new Thread(()->{
                            EnrollmentReport enrollmentReport=new EnrollmentReport();
                            enrollmentReport.setDate(enrollmentDate);
                            enrollmentReport.setStatus(folderPhotosAdapter.holderstatus.get(finalI));
                            enrollmentReport.setImageFile(items.get(finalI).toString());
                            enrollmentReportDao.insert(enrollmentReport);
                        }).start();
                    if(!exited[0]||alwaysNotify) {
                        handler.post(() -> {
                            folderPhotosAdapter.notifyItemChanged(finalI);
                            updateStats(infotext);
                        });
                    }
                    });
                }

                @Override
                public void onError(Exception exp) {
                    threadPool.submit(()->{
                    progres++;
                    error++;
                    completed[0] =true;
                    folderPhotosAdapter.holderstatus.put(finalI, "Error : Unable to get Face Features ");
                        new Thread(()->{
                            EnrollmentReport enrollmentReport=new EnrollmentReport();
                            enrollmentReport.setDate(enrollmentDate);
                            enrollmentReport.setStatus(folderPhotosAdapter.holderstatus.get(finalI));
                            enrollmentReport.setImageFile(items.get(finalI).toString());
                            enrollmentReportDao.insert(enrollmentReport);
                        }).start();
                    if(!exited[0]||alwaysNotify) {
                        handler.post(() -> {
                            folderPhotosAdapter.notifyItemChanged(finalI);
                            updateStats(infotext);
                        });
                    }
                    });
                }
            });

            processor.isReportSuccessNull = true;
            Bitmap myBitmap;
            try {
                if(folderPhotosAdapter.fetchedBitmaps.containsKey(finalI))
                    myBitmap=folderPhotosAdapter.fetchedBitmaps.get(finalI);
                else {
                    myBitmap = BitmapUtils.getPictureBitmap(items.get(i).getAbsolutePath());
                }

                if (myBitmap != null) {
                    processor.detectFace(myBitmap);
                } else {
                   completed[0]=true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                completed[0]=true;
                myBitmap=null;
            }
            //end of detecting

            Long accum=0L;
            try{
                while(!completed[0] &&accum<10000) {
                    accum+=200L;
                    Thread.sleep(200);
                }
            }catch (Exception ee)
            {
                ee.printStackTrace();
                //completed[0]=true;
            }
            pos=i+1;
            exited[0]=true;

            if(myBitmap!=null)
                myBitmap.recycle();
            myBitmap = null;
        }
    }

    private void updateStats(TextView infotext) {
        String str="Progress: "+progres+"/"+tot+" ; "+enrolled+" enrolled ; "+error+" error(s)";
        infotext.setText(str);
    }

    private void placeError(FolderPhotosAdapter.ViewHolder currentHolder, String str) {
        currentHolder.progressBar.setVisibility(View.GONE);
        currentHolder.card.setBackgroundColor(Color.RED);
        currentHolder.text.setText(str);
    }

    private void placeSuccess(FolderPhotosAdapter.ViewHolder currentHolder) {
        currentHolder.progressBar.setVisibility(View.GONE);
        currentHolder.card.setBackgroundColor(Color.GREEN);
        //currentHolder.text.setText(str);
    }



}