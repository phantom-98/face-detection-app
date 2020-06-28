package com.facecoolalert.ui.settings.logs;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.CrashLogDao;
import com.facecoolalert.database.entities.CrashLog;
import com.facecoolalert.utils.EditTextUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class CrashLogsFragment extends Fragment {

    private View view;


    private EditText search;

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_crash_logs,container,false);

        recyclerView=view.findViewById(R.id.logs_list);
        search=view.findViewById(R.id.search_logs);


        feedInfo();

        return view;
    }

    private CrashLogDao crashLogDao;
    private LogAdapter logAdapter;
    private void feedInfo() {
        crashLogDao= MyDatabase.getInstance(getContext()).crashLogDao();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setVerticalScrollBarEnabled(true);
        CompletableFuture.supplyAsync(()->{
            try{
                return crashLogDao.getAllCrashLogs();
            }catch (Exception es)
            {
                return Collections.emptyList();
            }
        }).thenAcceptAsync(crashLogs->{
            new Handler(Looper.getMainLooper()).post(()->{
                logAdapter=new LogAdapter(getContext(), (List<CrashLog>) crashLogs);

                recyclerView.setAdapter(logAdapter);

//                recyclerView.setBackgroundColor(Color.RED);
                EditTextUtils.addTextChangeListener(search,newText -> {
                    if(newText!=null)
                        logAdapter.query(newText);
                });
            });
        }, AsyncTask.THREAD_POOL_EXECUTOR);




    }


}
