package com.facecoolalert.ui.Alerts.Distribution;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.DistributionListDao;
import com.facecoolalert.database.entities.DistributionList;
import com.facecoolalert.ui.Alerts.AlertFragment;
import com.facecoolalert.ui.MainActivity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DistributionView {

    private View newView;
    private AlertFragment alertFragment;

    private RecyclerView recyclerView;

    private View addNew;

    public DistributionView(AlertFragment alertFragment) {
        this.alertFragment=alertFragment;
        if(alertFragment ==null|| alertFragment.playground==null)
            return;
        RelativeLayout playground= alertFragment.playground;
        playground.removeAllViews();
        this.newView= alertFragment.getLayoutInflater().inflate(R.layout.distributions_segment,null);
        newView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        playground.addView(newView);



        this.recyclerView=newView.findViewById(R.id.recycleView);
        this.addNew=newView.findViewById(R.id.add_distribution);

        addNew.setOnClickListener(v -> AlertFragment.mainActivity.addFragment(new AddDistributionFragment(alertFragment,null)));

        DistributionListDao distributionListDao= MyDatabase.getInstance(alertFragment.getContext()).distributionListDao();

        recyclerView.setLayoutManager(new LinearLayoutManager(alertFragment.getContext()));
        CompletableFuture.supplyAsync(()->{
            try{
                return distributionListDao.getAllDistributionLists();
            }catch (Exception es){
                es.printStackTrace();
                return Collections.emptyList();
            }
        }).thenAccept(lists->{
            recyclerView.setAdapter(new DistributionListsAdapter((List<DistributionList>) lists, alertFragment));
        });



    }
}
