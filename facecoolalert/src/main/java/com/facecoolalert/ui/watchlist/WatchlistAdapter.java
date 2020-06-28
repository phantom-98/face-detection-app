package com.facecoolalert.ui.watchlist;

import android.content.Context;
import android.os.AsyncTask;
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
import com.facecoolalert.database.entities.WatchList;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.watchlist.addWatchList.AddWatchList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder> {

    private MainActivity act;
    private SubjectDao subjectDao;
    private List<WatchList> watchLists;
    private Context context;

    private SimpleDateFormat dateFormat=new SimpleDateFormat("MMM d yyyy HH:mm:ss");

    private Handler uiHandler;

    WatchlistAdapter(Context context, List<WatchList> watchLists,MainActivity act)
    {
        this.context=context;
        this.act=act;
        this.watchLists=new ArrayList<>(watchLists);
        uiHandler=new Handler();
        this.subjectDao= MyDatabase.getInstance(context).subjectDao();
    }


    @NonNull
    @Override
    public WatchlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watchlist, parent, false);
        return new WatchlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchlistViewHolder holder, int position) {
        WatchList wl= watchLists.get(position);
        holder.nameTextView.setText(wl.getName());
        holder.typeTextView.setText(wl.getType());
        holder.createdTextView.setText(dateFormat.format(new Date(wl.getCreatedOn())));
        CompletableFuture.supplyAsync(()->{
            try {
                return subjectDao.countByWatchList(wl.getName());
            }catch (Exception es)
            {
                es.printStackTrace();
                return 0;
            }
        }).thenAcceptAsync(subjects -> {
            new Handler(Looper.getMainLooper()).post(()->{
                holder.subjectsTextView.setText(Integer.toString(subjects));
            });
        }, AsyncTask.THREAD_POOL_EXECUTOR);

        holder.editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.addFragment(new AddWatchList(wl));
            }
        });
    }

    @Override
    public int getItemCount() {
        return watchLists.size();
    }


    public class WatchlistViewHolder extends RecyclerView.ViewHolder{

        private TextView nameTextView;
        private TextView typeTextView;
        private TextView subjectsTextView;
        private TextView createdTextView;
        private ImageView editImageView;


        public WatchlistViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            typeTextView = itemView.findViewById(R.id.type);
            subjectsTextView = itemView.findViewById(R.id.subjects);
            createdTextView = itemView.findViewById(R.id.created);
            editImageView = itemView.findViewById(R.id.editImage);
        }
    }
}
