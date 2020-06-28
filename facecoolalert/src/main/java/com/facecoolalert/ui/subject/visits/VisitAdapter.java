package com.facecoolalert.ui.subject.visits;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.utils.DynamicResultsQueryGenerator;
import com.facecoolalert.utils.GenUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.ViewHolder> {

    private List<RecognitionResult> visitList;
    private List<RecognitionResult> masterList;

    private SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm:ss");

    private RecognitionResultDao recognitionResultDao;

    private Context context;

    private Subject subject;

    private String query;

    private int itemsCount;


    VisitAdapter(Context context)
    {
        this.subject=null;
        this.recognitionResultDao= MyDatabase.getInstance(context).recognitionResultDao();
        this.query="";
        itemsCount=0;
        threadPool= Executors.newSingleThreadExecutor();
    }

    public void setSubject(Subject subject)
    {
        this.subject=subject;
        new Thread(()->{
            itemsCount= recognitionResultDao.countRecognitionResultsByQuery(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getCountResultsBySubject(subject.getUid(),query)));
            new android.os.Handler(Looper.getMainLooper()).post(()->{
                notifyDataSetChanged();
            });
        }).start();
    }

    VisitAdapter(Context context,Subject subject) {
       this.subject=subject;
       this.recognitionResultDao= MyDatabase.getInstance(context).recognitionResultDao();
       this.query="";
        new Thread(()->{
            itemsCount= recognitionResultDao.countRecognitionResultsByQuery(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getCountResultsBySubject(subject.getUid(),query)));
        }).start();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_visit, parent, false);
        return new ViewHolder(view);
    }

    private ExecutorService threadPool;
    private HashMap<Integer,RecognitionResult> visitsCache=new HashMap();
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(visitsCache.containsKey(position))
        {
            RecognitionResult visit=visitsCache.get(position);
            holder.profilePictureImageView.setImageBitmap(visit.getBmp());
            holder.locationNameTextView.setText(visit.getLocation());
            Date vistDate = new Date(visit.getDate());
            holder.visitDateTextView.setText(dateFormat.format(vistDate));
            holder.visitTimeTextView.setText(timeFormat.format((vistDate)));
            holder.score.setText("Score : " + GenUtils.roundScore(visit.getScoreMatch()) + "%");
        }
        else {
            RecognitionResult visit = new RecognitionResult();
            holder.profilePictureImageView.setImageBitmap(visit.getBmp());
            holder.locationNameTextView.setText(visit.getLocation());

            Date vistDate = visit.getDate()==null?new Date():new Date(visit.getDate());

            holder.visitDateTextView.setText(dateFormat.format(vistDate));
            holder.visitTimeTextView.setText(timeFormat.format((vistDate)));
            holder.score.setText("Score : " + GenUtils.roundScore(visit.getScoreMatch()) + "%");


            final int finalPosition=position;
            threadPool.submit(() -> {
                Log.d("SubjectVisits","Fetching position "+finalPosition);
                if(!visitsCache.containsKey(finalPosition)) {
                    List<RecognitionResult> results =
                            recognitionResultDao.getRecognitionResultsByQuery(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getResultsBySubject(subject.getUid(), query, position, 10)));//visitList.get(position);
                    for (int i = 0; i < results.size(); i++)
                        visitsCache.put(i+finalPosition, results.get(i));

                    Log.d("SubjectVisits","Finished Fetching position "+finalPosition+" to "+(finalPosition+results.size()));
                    new Handler(Looper.getMainLooper()).post(() -> {
                        notifyDataSetChanged();
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        notifyItemChanged(finalPosition);
                    });
                    Log.d("SubjectVisits", "position " + finalPosition + " Already Handled");
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        //return visitList.size();
        return this.itemsCount;
    }

    public void filter(Date minDate, Date maxDate) {

        query="";

            if(minDate!=null)
                query+="date>="+minDate.getTime()+" and ";


            if(maxDate!=null)
                query+="date<="+maxDate.getTime()+" and ";

            try{
                threadPool.shutdown();
            }catch (Exception es)
            {
                es.printStackTrace();
            }
            threadPool=Executors.newSingleThreadExecutor();

        new Thread(()->{
            itemsCount= recognitionResultDao.countRecognitionResultsByQuery(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getCountResultsBySubject(subject.getUid(),query)));
            visitsCache.clear();
            new Handler(Looper.getMainLooper()).post(()->{
                notifyDataSetChanged();
            });
        }).start();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePictureImageView;
        ImageView locationIconImageView;
        TextView locationNameTextView;
        TextView visitDateTextView;
        TextView visitTimeTextView;

        TextView score;

        public ViewHolder(View itemView) {
            super(itemView);
            profilePictureImageView = itemView.findViewById(R.id.profile_picture);
            locationIconImageView = itemView.findViewById(R.id.location_icon);
            locationNameTextView = itemView.findViewById(R.id.location_name);
            visitDateTextView = itemView.findViewById(R.id.visit_date);
            visitTimeTextView = itemView.findViewById(R.id.visit_time);
            score=itemView.findViewById(R.id.score);
        }
    }
}
