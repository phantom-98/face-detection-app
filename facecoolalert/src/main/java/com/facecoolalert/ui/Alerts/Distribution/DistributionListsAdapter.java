package com.facecoolalert.ui.Alerts.Distribution;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubscriberDistributionListDao;
import com.facecoolalert.database.entities.DistributionList;
import com.facecoolalert.ui.Alerts.AlertFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DistributionListsAdapter extends RecyclerView.Adapter<DistributionListsAdapter.ViewHolder> {

    private  AlertFragment alertFragment;
    private List<DistributionList> dataList;

    public SubscriberDistributionListDao subscriberDistributionListDao;

    // Constructor to initialize the data list
    public DistributionListsAdapter(List<DistributionList> dataList, AlertFragment alertFragment) {
        this.dataList = dataList;
        this.alertFragment=alertFragment;
        this.subscriberDistributionListDao= MyDatabase.getInstance(alertFragment.getContext()).subscriberDistributionListDao();

    }

    // ViewHolder to hold views
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public TextView subscribersTextView;
        public TextView createdTextView;

        public ImageView editImageView;



        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            subscribersTextView = itemView.findViewById(R.id.subscribers);
            createdTextView = itemView.findViewById(R.id.created);

            editImageView = itemView.findViewById(R.id.editImage);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_distributionlist, parent, false);
        return new ViewHolder(itemView);
    }

    private SimpleDateFormat dateFormat=new SimpleDateFormat("MMM d yyyy HH:mm:ss");
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to the views within the ViewHolder
        DistributionList data = dataList.get(position);
        holder.nameTextView.setText(data.getName());

        try {
            holder.createdTextView.setText(dateFormat.format(new Date(data.getCreated())));
        }catch (Exception ews)
        {
            ews.printStackTrace();
        }

        holder.editImageView.setOnClickListener(v -> AlertFragment.mainActivity.addFragment(new AddDistributionFragment(alertFragment,data)));

        CompletableFuture.supplyAsync(()->{
            try{
                return subscriberDistributionListDao.countSubscribers(data.getUid());
            }catch (Exception es){
                es.printStackTrace();
                return 0;
            }
        }).thenAccept(subscribers->{
            holder.subscribersTextView.setText(subscribers+"");
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
