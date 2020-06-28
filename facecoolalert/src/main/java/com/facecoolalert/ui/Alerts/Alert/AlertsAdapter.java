package com.facecoolalert.ui.Alerts.Alert;

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
import com.facecoolalert.database.Repositories.DistributionListDao;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.database.entities.Alert;
import com.facecoolalert.ui.Alerts.AlertFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.ViewHolder> {

    private List<Alert> dataList;

    private WatchlistDao watchlistDao;

    private DistributionListDao distributionListDao;

    private AlertFragment alertFragment;

    // Constructor to initialize the data list
    public AlertsAdapter(List<Alert> dataList, AlertFragment alertFragment) {
        this.dataList = dataList;
        watchlistDao= MyDatabase.getInstance(alertFragment.getContext()).watchlistDao();
        distributionListDao=MyDatabase.getInstance(alertFragment.getContext()).distributionListDao();
        this.alertFragment=alertFragment;

    }

    // ViewHolder to hold views
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public TextView watchlistTextView;
        public TextView distributionlistTextView;
        public TextView locationTextView;
        public TextView createdTextView;
        public ImageView editImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            watchlistTextView = itemView.findViewById(R.id.watchlist);
            distributionlistTextView = itemView.findViewById(R.id.distributionlist);
            locationTextView = itemView.findViewById(R.id.location);
            createdTextView = itemView.findViewById(R.id.created);
            editImageView = itemView.findViewById(R.id.editImage);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new ViewHolder(itemView);
    }

    private SimpleDateFormat dateFormat=new SimpleDateFormat("MMM d yyyy HH:mm:ss");
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to the views within the ViewHolder
        Alert data = dataList.get(position);
        holder.nameTextView.setText(data.getName());

        new Thread(()->{
            String watchlist;
            try {
//                System.out.println("watchlist id "+data.getWatchlist());
                watchlist = watchlistDao.getByNum(data.getWatchlist_id()).getName();
            }catch (Exception e)
            {
                e.printStackTrace();
                watchlist=""+data.getWatchlist_id();
            }
            String distlist;
            try {
                distlist = distributionListDao.getDistributionListById(data.getDistributionList_id()).getName();
            }catch (Exception e)
            {
                e.printStackTrace();
                distlist=""+data.getDistributionList_id();

            }
            String finalWatchlist = watchlist;
            String finalDistlist = distlist;
            new Handler(Looper.getMainLooper()).post(()->{
                holder.watchlistTextView.setText(finalWatchlist.trim());
                holder.distributionlistTextView.setText(finalDistlist.trim());
            });
        }).start();


        holder.locationTextView.setText(data.getLocation());
        holder.createdTextView.setText(dateFormat.format(new Date(data.getCreated())));

        holder.editImageView.setOnClickListener(v -> AlertFragment.mainActivity.addFragment(new AddAlertFragment(alertFragment,data)));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
