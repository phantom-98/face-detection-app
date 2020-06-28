package com.facecoolalert.ui.Alerts.Subscribers;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubscriberDao;
import com.facecoolalert.database.entities.Subscriber;
import com.facecoolalert.ui.Alerts.AlertFragment;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SubscriberView {

    private View newView;
    private AlertFragment alertFragment;

    private RecyclerView recyclerView;

    private View addNew;

    public SubscriberView(AlertFragment alertFragment) {
        this.alertFragment=alertFragment;
        if(alertFragment==null||alertFragment.playground==null)
            return;
        RelativeLayout playground=alertFragment.playground;
        playground.removeAllViews();
        this.newView=alertFragment.getLayoutInflater().inflate(R.layout.subscribers_segment,null);
        newView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        playground.addView(newView);

        this.recyclerView=newView.findViewById(R.id.recycleView);
        this.addNew=newView.findViewById(R.id.add_subscriber);

        addNew.setOnClickListener(v -> AlertFragment.mainActivity.addFragment(new AddSubscriberFragment(alertFragment,null)));

        SubscriberDao subscriberDao= MyDatabase.getInstance(alertFragment.getContext()).subscriberDao();

        recyclerView.setLayoutManager(new LinearLayoutManager(alertFragment.getContext()));
        CompletableFuture.supplyAsync(()->{
            try {
                return subscriberDao.getAllSubscribers();
            }catch (Exception es)
            {
                es.printStackTrace();
                return Collections.emptyList();
            }
        }).thenAccept(subscribers->{
            recyclerView.setAdapter(new SubscribersAdapter((List<Subscriber>) subscribers,alertFragment));
        });
    }
}
