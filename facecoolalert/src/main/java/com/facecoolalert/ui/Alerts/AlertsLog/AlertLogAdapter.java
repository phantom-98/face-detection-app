package com.facecoolalert.ui.Alerts.AlertsLog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.AlertLogDao;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.AlertLog;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.utils.GenUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class AlertLogAdapter extends RecyclerView.Adapter<AlertLogAdapter.AlertLogViewHolder> {
    private Context context;
    private int all;

    private RecognitionResultDao recognitionResultDao;

    private SubjectDao subjectDao;

    private AlertLogDao alertLogDao;

    private ExecutorService threadPool;

    private HashMap<Integer,AlertLog> alertsCache=new HashMap<>();

    public AlertLogAdapter(int all, Context context) {
        this.all=all;
        this.context=context;

        recognitionResultDao= MyDatabase.getInstance(context).recognitionResultDao();
        subjectDao=MyDatabase.getInstance(context).subjectDao();
        alertLogDao=MyDatabase.getInstance(context).alertLogDao();
        threadPool= Executors.newSingleThreadExecutor();
    }

    @NonNull
    @Override
    public AlertLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert_log, parent, false);
        return new AlertLogViewHolder(view);
    }

    private SimpleDateFormat dateFormat=new SimpleDateFormat("MMM d yyyy HH:mm:ss");

    @Override
    public void onBindViewHolder(@NonNull AlertLogViewHolder holder, int position) {
        //set null data
        holder.visitPicture.setImageBitmap(null);
        holder.subjectPicture.setImageBitmap(null);
        holder.locationNameTextView.setText("");
        holder.scoreTextView.setText("");
        holder.alertNameTextView.setText("");
        holder.createdNameTextView.setText("");
        //end of setting null data
        final Long index= Long.valueOf(position);
        threadPool.submit(()->{
            try {
                AlertLog alertLog = alertLogDao.getAlertLogByPosition(index);
                RecognitionResult recognitionResult = recognitionResultDao.getByUid(alertLog.getRecognitionResult_id());
                Subject subject = subjectDao.getSubjectByuid(recognitionResult.getSubjectId());

                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        holder.visitPicture.setImageBitmap(recognitionResult.getBmp());
                        holder.subjectPicture.setImageBitmap(subject.loadImage(context));
                        holder.locationNameTextView.setText(recognitionResult.getLocation());
                        holder.scoreTextView.setText(GenUtils.roundScore(recognitionResult.getScoreMatch()) + "");
                        holder.alertNameTextView.setText(alertLog.getAlertName());
                        holder.createdNameTextView.setText(dateFormat.format(new Date(recognitionResult.getDate())));
                    }catch (Exception erer)
                    {
                        erer.printStackTrace();
                        holder.subjectPicture.setImageResource(R.drawable.profile);
                    }
                });
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        });
    }

    @Override
    public int getItemCount() {
        return all;
    }


    public class AlertLogViewHolder extends RecyclerView.ViewHolder {

        CircleImageView visitPicture;
        CircleImageView subjectPicture;
        TextView createdNameTextView;
        TextView locationNameTextView;
        TextView scoreTextView;
        TextView alertNameTextView;

        public AlertLogViewHolder(@NonNull View itemView) {
            super(itemView);


            visitPicture = itemView.findViewById(R.id.visit_picture);
            subjectPicture = itemView.findViewById(R.id.subject_picture);
            createdNameTextView = itemView.findViewById(R.id.created);
            locationNameTextView = itemView.findViewById(R.id.location_name);
            scoreTextView = itemView.findViewById(R.id.score);
            alertNameTextView = itemView.findViewById(R.id.alert);
        }
    }
}
