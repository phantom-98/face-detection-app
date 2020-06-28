package com.facecoolalert.ui.settings.logs;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.App;
import com.facecoolalert.R;
import com.facecoolalert.database.entities.CrashLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final Context context;
    private List<CrashLog> crashLogList;

    private List<CrashLog> master;

    public LogAdapter(Context context, List<CrashLog> crashLogList) {
        this.context = context;
        this.master=crashLogList;
        this.crashLogList = new ArrayList<>();
       this.crashLogList.addAll(crashLogList);
//        for(CrashLog i:crashLogList)
//            crashLogList.add(i);

    }

    public void query(String str)
    {
        crashLogList.clear();
        this.crashLogList = new ArrayList<>();
        for(CrashLog i:master)
            if((i.getTitle()!=null&&i.getContent()!=null)&&(i.getTitle().toLowerCase().contains(str.toLowerCase())||i.getContent().toLowerCase().contains(str.toLowerCase())))
                crashLogList.add(i);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crash_log, parent, false);
        return new LogViewHolder(view);
    }
    private SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm:ss");

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        CrashLog crashLog = crashLogList.get(position);
        String contentString=crashLog.getContent();
        if(contentString.length()>12566)
            contentString=contentString.substring(0,12566);

        holder.logTitle.setText(crashLog.getTitle());
        holder.visitDate.setText(dateFormat.format(new Date(crashLog.getDate())));
        holder.visitTime.setText(timeFormat.format(new Date(crashLog.getDate())));
        holder.logContent.setText(contentString);
        holder.logResources.setText(crashLog.getResourcesStatus());



        holder.logDetails.setVisibility(View.GONE);
        holder.uploadLogButton.setBackgroundColor(Color.parseColor("#132558"));

        // Set click listener for logButton to toggle logDetails visibility
        holder.logButton.setOnClickListener(v -> {
            int visibility = holder.logDetails.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            holder.logDetails.setVisibility(visibility);
        });

        holder.uploadLogButton.setOnClickListener((v)->{
            App.recordException(new Throwable(crashLog.getContent()));
            Toast.makeText(context, "Sent Successfully", Toast.LENGTH_SHORT).show();
            v.setBackgroundColor(Color.CYAN);
        });
    }

    @Override
    public int getItemCount() {
        return crashLogList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        RelativeLayout logButton;
        TextView logTitle;
        TextView visitDate;
        TextView visitTime;
        RelativeLayout logDetails;
        TextView logContent;
        Button uploadLogButton;
        TextView logResources;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card);
            logButton = itemView.findViewById(R.id.logButton);
            logTitle = itemView.findViewById(R.id.log_title);
            visitDate = itemView.findViewById(R.id.crash_date);
            visitTime = itemView.findViewById(R.id.crash_time);
            logDetails = itemView.findViewById(R.id.logDetails);
            logContent = itemView.findViewById(R.id.logContent);
            uploadLogButton = itemView.findViewById(R.id.uploadLog);
            logResources = itemView.findViewById(R.id.logResources);
        }
    }
}
