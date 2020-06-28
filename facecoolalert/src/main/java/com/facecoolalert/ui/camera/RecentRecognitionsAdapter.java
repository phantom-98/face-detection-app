package com.facecoolalert.ui.camera;

import static com.facecoolalert.ui.camera.CameraFragment.SUBJECTCARD_REQUEST_CODE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.databinding.ItemSmallCameraDetectionBinding;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.ui.subject.enrollments.EnrollSubject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class RecentRecognitionsAdapter extends RecyclerView.Adapter<RecentRecognitionsAdapter.DetectionItemHolder> {
    private ArrayList<DetectItem> mListItems;

    public Long lastDataChange = 0L;
    public Long lastUpdate = 0L;

    private CameraFragment cameraFragment;

    private static int stripSize=15;
    private static DetectItem[] items=new DetectItem[stripSize];



    public static ConcurrentLinkedQueue<RecognitionResult> queue=new ConcurrentLinkedQueue<>();

    public RecentRecognitionsAdapter(CameraFragment cameraFragment) {
        mListItems = new ArrayList<>();
        Long updateInterval = 500L;
        this.cameraFragment = cameraFragment;



//
        // Schedule periodic data refresh every second
        ScheduledExecutorService executorService= Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(()->{
            if(cameraFragment.isAdded()) {
                try {
                    if (!queue.isEmpty()) {
                        Log.d("Bottom Strip ", "Queue : " + queue.toString());
                        while (queue.size()>stripSize)
                            queue.poll();
                        int queueSize = queue.size();
                        for (int i = stripSize - 1; i >= queueSize; i--) {
                            items[i] = items[i - queueSize];
                            Log.d("Bottom Strip ", "move index  : " + (i - queueSize) + " to :  " + i + "  Name : " + (items[i].subject==null?"null":items[i].subject.getName()) + " Id : " + items[i].ref);
                        }


                        for (int j = queueSize - 1; j >= 0 && !queue.isEmpty(); j--) {
                            RecognitionResult result = queue.poll();
                            if (result.getSubjectId() == null) {
                                DetectItem myItem = DetectedItemsItemsUtils.createDetection(result.genFaceData());
                                myItem.ref = result.getUid();
                                items[j] = myItem;
                                Log.d("Bottom Strip ", "Insert on Index " + j + " Id : " + items[j].ref);
                            } else {
                                DetectItem detectItem = new DetectItem();
                                try {
                                    detectItem.subject = (Subject) result.getFaceData().subject;
                                } catch (Exception es) {
//                                    es.printStackTrace();
                                }
//                                detectItem.subject.profilePhoto = result.getBmp();
                                //detectItem.ref = result.getNum();
                                if (detectItem.subject != null)
                                    detectItem.subject.profilePhoto = result.getBmp();
                                else {
                                    detectItem = DetectedItemsItemsUtils.createDetection(result.genFaceData());
                                    detectItem.ref = result.getUid();
                                }
                                items[j] = detectItem;
                                Log.d("Bottom Strip ", "Insert on Index " + j);
                            }

                        }
                        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                    }
                } catch (Exception es) {
                    es.printStackTrace();
                }
            }
            else
                executorService.shutdown();

            }
                ,0,updateInterval,TimeUnit.MILLISECONDS);

        if(items[0]==null) {
            RecognitionResultDao recognitionResultDao = MyDatabase.getInstance(cameraFragment.getContext()).recognitionResultDao();
            SubjectDao subjectDao = MyDatabase.getInstance(cameraFragment.getContext()).subjectDao();
            //load initial data first Time;
            CompletableFuture.supplyAsync(() -> {
                try {
                    Long start=System.currentTimeMillis();
                    List<String> previousIds=recognitionResultDao.getRecentUids(stripSize);
                    List<RecognitionResult> allRecognitionResult = new LinkedList<>();//recognitionResultDao.getAllRecognitionResult(stripSize);
                    for(String uid:previousIds)
                        allRecognitionResult.add(recognitionResultDao.getByUid(uid));
                    Long stop=System.currentTimeMillis();
                    Long duration=stop-start;
                    Log.d("Fetch Rescent Recognitions","query fetched " + stripSize+" Duration : "+duration);
                    return allRecognitionResult;
                } catch (Exception es) {
                    return new ArrayList<RecognitionResult>();
                }
            }).thenAcceptAsync(results -> {
                for (RecognitionResult result : results) {
                    try {
                        if(result.getSubjectId()!=null)
                            result.setSubject(subjectDao.getSubjectByuid(result.getSubjectId()));
                        queue.offer(result);
                    } catch (Exception es) {
                            es.printStackTrace();
                    }
                }
                new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
            }, AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }



    @Override
    public DetectionItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DetectionItemHolder(ItemSmallCameraDetectionBinding.inflate(cameraFragment.getLayoutInflater()));
    }

    @Override
    public void onBindViewHolder(DetectionItemHolder holder, int position) {
        try {
            holder.bind(items[position]);
        } catch (Exception e) {

        }
    }

    @Override
    public int getItemCount() {
        return stripSize;
    }


    class DetectionItemHolder extends RecyclerView.ViewHolder {

        ItemSmallCameraDetectionBinding binding;

        public DetectionItemHolder(ItemSmallCameraDetectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DetectItem item) {
            binding.setDetectionItem(item);
            binding.getRoot().setSelected(item.subject != null);

            if (item.subject != null) {
                Bitmap image = item.subject.profilePhoto;
                if (image == null)
                    image = item.subject.loadImage(binding.card.getContext());
                ((ImageView) binding.card.findViewById(R.id.image_camera_detection)).setImageBitmap(image);
            } else {
                ((ImageView) binding.card.findViewById(R.id.image_camera_detection)).setImageResource(R.drawable.ic_default_avatar);
            }

            if (item.getName().startsWith("?")) {
                ((TextView) binding.card.findViewById(R.id.tv_user_name)).setTextColor(Color.RED);
                binding.card.setCardBackgroundColor(Color.RED);
                ((TextView) binding.card.findViewById(R.id.tv_user_name)).setTextSize(14);
            } else {
                ((TextView) binding.card.findViewById(R.id.tv_user_name)).setTextColor(Color.parseColor("#013220"));
                binding.card.setCardBackgroundColor(Color.GREEN);
                ((TextView) binding.card.findViewById(R.id.tv_user_name)).setTextSize(10);
            }


            if (item.subject != null && item.ref != null) {
                binding.card.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), EnrollSubject.class);
                    intent.putExtra("RecognitionResult", item.ref);
                    //startActivity(intent);
                    cameraFragment.getActivity().startActivityForResult(intent, SUBJECTCARD_REQUEST_CODE);
                });
            } else {
                binding.card.setOnClickListener(null);
            }
        }
    }
}