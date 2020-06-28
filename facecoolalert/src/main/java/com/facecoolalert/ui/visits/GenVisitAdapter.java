package com.facecoolalert.ui.visits;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.facecool.attendance.Constants;
import com.facecoolalert.R;
import com.facecoolalert.common.imageSavePreview.SaveImagePreviewDialog;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.ui.subject.enrollments.EnrollSubject;
import com.facecoolalert.ui.subject.enrollments.SubjectCard;
import com.facecoolalert.utils.DynamicResultsQueryGenerator;
import com.facecoolalert.utils.GenUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class GenVisitAdapter extends RecyclerView.Adapter<GenVisitAdapter.ViewHolder> {

    public static int SUBJECTCARD_REQUEST_CODE = 168;
    private Context context;
    private long maxDate;
    private List<RecognitionResult> visitList;
    private List<RecognitionResult> masterList;

    private SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm:ss");

    private RecognitionResultDao recognitionResultDao;

    private SubjectDao subjectDao;

    private int count;

    private String query;

    private HashMap<Integer,RecognitionResult> visitsCache;
    private HashMap<String, Subject> subjectsCache;
    ExecutorService executorService;

    private static HashSet<Integer> workingOn=new HashSet<>();

    public RecyclerView recyclerView;

    public LinearLayoutManager linearLayoutManager;

    public FetchDataHelper fetchDataHelper;

    private VisitsFragment visitsFragment;

    GenVisitAdapter(Context context, long maxDate) {

        this.maxDate=maxDate;
        this.context=context;

        this.recognitionResultDao=MyDatabase.getInstance(context).recognitionResultDao();
        this.subjectDao= MyDatabase.getInstance(context).subjectDao();

        query="date<="+maxDate+" and ";
        new Thread(()->{
//            count= recognitionResultDao.countUpTo(maxDate);
            count=recognitionResultDao.countRecognitionResultsByQuery(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getCountResultsByQuery(query)));
        }).start();
//        visitsCache=new HashMap<>();
        subjectsCache=new HashMap<String, Subject>();
        executorService= Executors.newSingleThreadExecutor();

        fetchDataHelper=new FetchDataHelper(recognitionResultDao,query,recyclerView,this);
        fetchDataHelper.start();

    }


    GenVisitAdapter(Context context)
    {
        this.maxDate=new Date().getTime();
        this.context=context;

        this.recognitionResultDao=MyDatabase.getInstance(context).recognitionResultDao();
        this.subjectDao= MyDatabase.getInstance(context).subjectDao();

        this.count=0;
//        visitsCache=new HashMap<>();
        subjectsCache=new HashMap<String, Subject>();
        executorService= //Executors.newCachedThreadPool();
        Executors.newSingleThreadExecutor();
//        Executors.newFixedThreadPool(5);

    }

    public GenVisitAdapter(Context context, RecyclerView visitsRecycleView) {
        this(context);
        this.recyclerView=visitsRecycleView;
        fetchDataHelper=new FetchDataHelper(recognitionResultDao,query,recyclerView, this);
//        fetchDataHelper.start();
    }

    public GenVisitAdapter(Context context, RecyclerView visitsRecycleView, VisitsFragment visitsFragment) {
        this(context,visitsRecycleView);
        this.visitsFragment=visitsFragment;

    }

    public void setMaxDate(Long maxDate)
    {
//        visitsCache.clear();
        this.maxDate=maxDate;
        query="date<="+maxDate+" and ";
        fetchDataHelper.doStop();
        fetchDataHelper=new FetchDataHelper(recognitionResultDao,query,recyclerView, this);
        new Thread(()->{
//            count= recognitionResultDao.countUpTo(maxDate);
            count=recognitionResultDao.countRecognitionResultsByQuery(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getCountResultsByQuery(query)));

            new Handler(Looper.getMainLooper()).post(()->{
                notifyDataSetChanged();
            });
        }).start();
        fetchDataHelper.start();
    }

    public void setQuery(String query)
    {
//        visitsCache.clear();
       this.query=query;
       try {
           executorService.shutdownNow();
       }catch (Exception es)
       {

       }
        fetchDataHelper.doStop();
        fetchDataHelper=new FetchDataHelper(recognitionResultDao,query,recyclerView, this);
       executorService=Executors.newSingleThreadExecutor();
        new Thread(()->{
//            count= recognitionResultDao.countUpTo(maxDate);
            count=recognitionResultDao.countRecognitionResultsByQuery(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getCountResultsByQuery(query)));

            System.out.println("query Results are "+count);
            new Handler(Looper.getMainLooper()).post(()->{

                notifyDataSetChanged();
            });
        }).start();
        fetchDataHelper.start();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(!fetchDataHelper.contains(position))
        {
            String loadingText="";
            holder.visitPictureImageView.setImageBitmap(null);
            holder.progres.setVisibility(View.VISIBLE);
            holder.locationNameTextView.setText(loadingText);

            holder.visitDateTextView.setText(loadingText);
            holder.visitTimeTextView.setText(loadingText);
            holder.subjectPictureImageView.setImageBitmap(null);//tmp.loadImage(holder.itemView.getContext()));
            holder.scoreTextView.setText(loadingText);
            holder.watchlistTextView.setText(loadingText);
            holder.subjectNameTextView.setText(loadingText);
            holder.scoreTextView.setVisibility(View.INVISIBLE);
            holder.subjectNameTextView.setTextColor(Color.RED);

            holder.itemView.setOnClickListener(null);
            holder.visitPictureImageView.setOnClickListener(null);
            holder.subjectPictureImageView.setOnClickListener(null);
        }
        else{
            RecognitionResult visit=fetchDataHelper.getResult(position);
            holder.visitPictureImageView.setImageBitmap(visit.getBmp());
            holder.progres.setVisibility(View.GONE);
            holder.locationNameTextView.setText(visit.getLocation());

            Date vistDate = new Date(visit.getDate());

            holder.visitDateTextView.setText(dateFormat.format(vistDate));
            holder.visitTimeTextView.setText(timeFormat.format((vistDate)));

            //load above subject name
            try {
                executorService.submit(() ->
                {
                    Subject subject = null;
                    if (visit.getSubjectId() != null) {
                        if (subjectsCache.containsKey(visit.getSubjectId()))
                            subject = subjectsCache.get(visit.getSubjectId());
                        else {
                            subject = subjectDao.getSubjectByuid(visit.getSubjectId());
                            subjectsCache.put(visit.getSubjectId(), subject);
                        }

                    }
                    Subject finalSubject1 = subject;
                    Subject finalSubject2 = subject;
                    new Handler(Looper.getMainLooper()).post(()->{

                    if (finalSubject1 == null) {
                        holder.scoreTextView.setVisibility(View.INVISIBLE);
                        holder.subjectNameTextView.setText(Constants.Name_NOTIDENTIFIED);
                        holder.subjectNameTextView.setTextColor(Color.RED);
                        holder.subjectPictureImageView.setImageResource(R.drawable.person);
                        holder.watchlistTextView.setText("");
                        if (visit.getSubjectId() != null && !visit.getSubjectId().isEmpty())
                            holder.subjectNameTextView.setText("Deleted Subject");

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, EnrollSubject.class);
                                intent.putExtra("RecognitionResult", visit.getUid());
                                //startActivity(intent);
                                ((Activity) context).startActivityForResult(intent, SUBJECTCARD_REQUEST_CODE);
                            }
                        });
                    } else {
                        holder.subjectPictureImageView.setImageBitmap(finalSubject1.loadImage(holder.itemView.getContext()));
                        holder.scoreTextView.setText("Score : " + GenUtils.roundScore(visit.getScoreMatch()) + " %");
                        holder.watchlistTextView.setText(finalSubject1.getWatchlist());
                        holder.subjectNameTextView.setText(finalSubject1.getName());
                        holder.scoreTextView.setVisibility(View.VISIBLE);

                        holder.subjectNameTextView.setTextColor(Color.parseColor("#000000"));
                        Subject finalSubject = finalSubject1;
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, SubjectCard.class);
                                intent.putExtra("subject", finalSubject.getUid());

                                ((Activity) v.getContext()).startActivityForResult(intent, SUBJECTCARD_REQUEST_CODE);
                            }
                        });
                    }
                    holder.visitPictureImageView.setOnClickListener(v -> {
                        SaveImagePreviewDialog saveImagePreviewDialog=new SaveImagePreviewDialog(visitsFragment.getActivity());
                        saveImagePreviewDialog.show(visit.getBmp(),"RecognitionResult "+(finalSubject2 !=null? finalSubject2.getName():Constants.Name_NOTIDENTIFIED));
                    });

                    if(finalSubject2!=null)
                    {
                        holder.subjectPictureImageView.setOnClickListener(v -> {
                            SaveImagePreviewDialog saveImagePreviewDialog=new SaveImagePreviewDialog(visitsFragment.getActivity());
                            saveImagePreviewDialog.show(finalSubject2.profilePhoto,finalSubject2.getName()+" Profile Photo");
                        });
                    }
                    });
                });
            }catch (Exception es)
            {
            }
        }
    }

    public void stop()
    {
        try {
            System.out.println("Shutting down adapter");
            executorService.shutdown();
            fetchDataHelper.doStop();
//            fetchDataHelper=new FetchDataHelper(recognitionResultDao,query,recyclerView);
        }catch (Exception ed)
        {
            ed.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return this.count;
    }

    public void filter(Date minDate, Date maxDate) {
        visitsCache.clear();
        visitList.clear();
        for(RecognitionResult item:masterList)
        {
            if(minDate!=null)
                if(item.getDate()<minDate.getTime())
                    continue;

            if(maxDate!=null)
                if(item.getDate()>maxDate.getTime())
                    continue;

            visitList.add(item);
        }
        notifyDataSetChanged();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView visitPictureImageView;
        public CircleImageView subjectPictureImageView;
        public TextView subjectNameTextView;
        public TextView scoreTextView;
        public ImageView locationIconImageView;
        public TextView locationNameTextView;
        public TextView watchlistTextView;
        public TextView visitDateTextView;
        public TextView visitTimeTextView;

        public ProgressBar progres;

        public ViewHolder(View itemView) {
            super(itemView);
            visitPictureImageView = itemView.findViewById(R.id.visit_picture);
            subjectPictureImageView = itemView.findViewById(R.id.subject_picture);
            subjectNameTextView = itemView.findViewById(R.id.subject_name);
            scoreTextView = itemView.findViewById(R.id.score);
            locationIconImageView = itemView.findViewById(R.id.location_icon);
            locationNameTextView = itemView.findViewById(R.id.location_name);
            watchlistTextView = itemView.findViewById(R.id.watchlist);
            visitDateTextView = itemView.findViewById(R.id.visit_date);
            visitTimeTextView = itemView.findViewById(R.id.visit_time);
            progres=itemView.findViewById(R.id.progres);
        }
    }
}
