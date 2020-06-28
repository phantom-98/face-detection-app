package com.facecoolalert.ui.settings.logs;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;

import java.util.ArrayList;
import java.util.List;

public class OtherLogsAdapter extends RecyclerView.Adapter<OtherLogsAdapter.LogViewHolder> {

    private final Context context;
    private List<String> logList;

    private List<String> master;

    public OtherLogsAdapter(Context context ) {
        this.context = context;
        this.master=new ArrayList<>();
        this.logList=new ArrayList<>();
    }

    public void query(String str)
    {
        logList.clear();
        this.logList = new ArrayList<>();
        for(String i:master)
            if((i.toLowerCase().contains(str.toLowerCase())))
                logList.add(i);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {

        String fullString=logList.get(position);
        String contentString=fullString;
        if(contentString.length()>12566)
            contentString=contentString.substring(0,12566);


        String title=contentString;
        if(contentString.length()>160)
            title = contentString.substring(0,160);
        int atPos=title.indexOf("at ");

        if(atPos>5)
            title=title.substring(0,atPos);

        holder.logTitle.setText(title);

        holder.logContent.setText(contentString);


        holder.logDetails.setVisibility(View.GONE);
        holder.uploadLogButton.setBackgroundColor(Color.parseColor("#132558"));

        // Set click listener for logButton to toggle logDetails visibility
        holder.logButton.setOnClickListener(v -> {
            int visibility = holder.logDetails.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            holder.logDetails.setVisibility(visibility);
        });


        holder.uploadLogButton.setOnClickListener((v)->{
//            App.recordLog(finalContentString);
            try {
                int IndexE=fullString.indexOf("E");
                String stackTraceString=fullString.substring(IndexE);
                int indexColon=stackTraceString.indexOf(":");
                stackTraceString=stackTraceString.substring(indexColon+1).trim();
                Log.d("Upload Log","Uploading this stackTrace",new Throwable(stackTraceString));
                App.recordException(new Throwable(stackTraceString));
            }catch (Exception es)
            {
                es.printStackTrace();
                App.recordLog(fullString);
            }
            Toast.makeText(context, "Sent Successfully", Toast.LENGTH_SHORT).show();
            v.setBackgroundColor(Color.CYAN);
        });
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public void add(String line) {
        master.add(0,line);
        logList.add(0,line);
        new Handler(Looper.getMainLooper()).post(()->{
            notifyItemChanged(0);
            for(int i=1;i<logList.size()&&i<10&&logList.size()%10==0;i++)
                notifyItemChanged(i);
        });
    }

    public void notifyAllChanged() {
        new Handler(Looper.getMainLooper()).post(()->{
            notifyDataSetChanged();
        });
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        RelativeLayout logButton;
        TextView logTitle;

        RelativeLayout logDetails;
        TextView logContent;
        Button uploadLogButton;
        TextView logResources;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card);
            logButton = itemView.findViewById(R.id.logButton);
            logTitle = itemView.findViewById(R.id.log_title);
            logDetails = itemView.findViewById(R.id.logDetails);
            logContent = itemView.findViewById(R.id.logContent);
            uploadLogButton = itemView.findViewById(R.id.uploadLog);
            logResources = itemView.findViewById(R.id.logResources);
        }
    }
}
