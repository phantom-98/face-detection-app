package com.facecoolalert.ui.Alerts.Alert;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.AlertDao;
import com.facecoolalert.database.entities.Alert;
import com.facecoolalert.ui.Alerts.AlertFragment;
import com.facecoolalert.ui.MainActivity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AlertView {

    private View newView;
    private AlertFragment alertFragment;

    private RecyclerView recyclerView;

    private View addNew;


    public AlertView(AlertFragment alertFragment) {
        this.alertFragment = alertFragment;
        if(alertFragment ==null|| alertFragment.playground==null)
            return;
        RelativeLayout playground= alertFragment.playground;
        playground.removeAllViews();
        this.newView= alertFragment.getLayoutInflater().inflate(R.layout.alerts_segment,null);
        newView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        playground.addView(newView);

        this.recyclerView=newView.findViewById(R.id.recycleView);
        this.addNew=newView.findViewById(R.id.add_new_alert);

        addNew.setOnClickListener(v -> AlertFragment.mainActivity.addFragment(new AddAlertFragment(alertFragment,null)));

        AlertDao alertDao= MyDatabase.getInstance(alertFragment.getContext()).alertDao();
        recyclerView.setLayoutManager(new LinearLayoutManager(alertFragment.getContext()));
        CompletableFuture.supplyAsync(()->{
            try{
                return alertDao.getAllAlerts();
            }catch (Exception es){
                es.printStackTrace();
                return Collections.emptyList();
            }
        }).thenAccept(subscribers->{
            recyclerView.setAdapter(new AlertsAdapter((List<Alert>) subscribers, alertFragment));
        });



    }
}
