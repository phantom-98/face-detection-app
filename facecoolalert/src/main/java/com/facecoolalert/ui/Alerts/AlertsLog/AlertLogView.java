package com.facecoolalert.ui.Alerts.AlertsLog;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.AlertLogDao;
import com.facecoolalert.ui.Alerts.AlertFragment;

import java.util.concurrent.CompletableFuture;

public class AlertLogView {

    private View newView;
    private AlertFragment alertFragment;

    private RecyclerView recyclerView;

    public AlertLogView(AlertFragment alertFragment) {
        this.alertFragment=alertFragment;
        if(alertFragment==null||alertFragment.playground==null)
            return;
        RelativeLayout playground=alertFragment.playground;
        playground.removeAllViews();
        this.newView=alertFragment.getLayoutInflater().inflate(R.layout.alert_log_segment,null);
        newView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        playground.addView(newView);


        this.recyclerView=newView.findViewById(R.id.recycleView);

        feedData();
    }

    private void feedData() {
        AlertLogDao alertLogDao= MyDatabase.getInstance(alertFragment.getContext()).alertLogDao();
        CompletableFuture.supplyAsync(()->{
            try{
                return alertLogDao.countAll();
            }catch (Exception es)
            {
                es.printStackTrace();
                return 0;
            }
        }).thenAccept(count->{
            recyclerView.setLayoutManager(new LinearLayoutManager(alertFragment.getContext()));
            recyclerView.setAdapter(new AlertLogAdapter(count,alertFragment.getContext()));
        });



    }
}
