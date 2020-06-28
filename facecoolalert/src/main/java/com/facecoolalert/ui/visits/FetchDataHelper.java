package com.facecoolalert.ui.visits;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.utils.DynamicResultsQueryGenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FetchDataHelper extends Thread{

    private GenVisitAdapter adapter;
    public HashMap<Integer, RecognitionResult> visitsCache;

    private RecognitionResultDao recognitionResultDao;

    private String query;

    private Boolean stopped=false;



    public FetchDataHelper(RecognitionResultDao recognitionResultDao, String query, RecyclerView currentRecycleView, GenVisitAdapter genVisitAdapter) {
        super();
        this.visitsCache = new HashMap<>();
        this.recognitionResultDao = recognitionResultDao;
        this.query = query;
        this.currentRecycleView = currentRecycleView;
        this.adapter=genVisitAdapter;
    }

    private RecyclerView currentRecycleView;




    FetchDataHelper()
    {
        super();

    }


   public void doStop()
   {
       visitsCache.clear();
       this.stopped=true;
       try {
           stop();
       }catch (Exception st)
       {

       }
   }

    @Override
    public void run()
    {

        try {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) currentRecycleView.getLayoutManager();
            Thread.sleep(500);
            int trials=0;
            while(!currentRecycleView.isShown()&&trials<3) {
                Thread.sleep(1000);
                trials++;
            }
            while (!stopped&&currentRecycleView.isShown()&&linearLayoutManager!=null) {
                try {
                    Thread.sleep(1000);
                    Log.d("Fetch Data Helper","Running "+getId());
                    //find missing caches
                    int start = -1;
                    for (int pos = linearLayoutManager.findFirstVisibleItemPosition(); pos <= linearLayoutManager.findLastVisibleItemPosition() && start == -1; pos++) {
                        if (!visitsCache.containsKey(pos))
                            start = pos;
                    }
                    if (start > -1&&!visitsCache.containsKey(start)) {
                        int fetchnum = linearLayoutManager.findLastVisibleItemPosition() - start;
                        fetchnum=50;
                        start=start-start%fetchnum;
                        Log.d("Fetch Data Helper","start of query from "+start+" Thread : "+getId());
                        Long startTime=System.currentTimeMillis();
                        SimpleSQLiteQuery qr = new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getRangeResultUidsByQuery(query, start, fetchnum));
                        List<String> resultIds=recognitionResultDao.getResultsUidsByQuery(qr);
                        List<RecognitionResult> tmpvisits = new LinkedList<>();
                        for(String uid:resultIds)
                            tmpvisits.add(recognitionResultDao.getByUid(uid));

                        Long endTime=System.currentTimeMillis();
                        Long duration=endTime-startTime;
                        Log.d("Fetch Data Helper","query fetched " + fetchnum+" Duration : "+duration);
                        for (int i = 0; i < tmpvisits.size(); i++) {
                            int position = start + i;
                            visitsCache.put(position, tmpvisits.get(i));
                        }
                        new Handler(Looper.getMainLooper()).post(() -> {
                            currentRecycleView.getAdapter().notifyDataSetChanged();
                        });
                    }
                } catch (Exception es) {
                    es.printStackTrace();
                }
            }
        }catch (Exception es)
        {
            es.printStackTrace();
        }

    }


    public Boolean contains(int position)
    {
        return visitsCache.containsKey(position);
    }

    public RecognitionResult getResult(int position)
    {
        return visitsCache.get(position);
    }
}
