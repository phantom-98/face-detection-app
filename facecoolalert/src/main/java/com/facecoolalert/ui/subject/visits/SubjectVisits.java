package com.facecoolalert.ui.subject.visits;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.utils.datetimepicker.DatePickerUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SubjectVisits extends AppCompatActivity {

    private Subject subject;

    private EditText fromDate;

    private EditText toDate;

    private RecyclerView visitsView;

    private VisitAdapter visitAdapter;

    private  RecognitionResultDao recognitionResultDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_visits);

        View backButton=findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recognitionResultDao=MyDatabase.getInstance(this).recognitionResultDao();
        Intent verificationIntent=getIntent();
        if(verificationIntent.hasExtra("subject"))
        {
                feedData(verificationIntent.getStringExtra("subject"));
        }

        fromDate=findViewById(R.id.fromDate);
        toDate=findViewById(R.id.toDate);

        DatePickerUtils fromDateUtils=new DatePickerUtils(fromDate);
        DatePickerUtils toDateUtils=new DatePickerUtils(toDate);

        visitsView=findViewById(R.id.visits);

        visitAdapter=new VisitAdapter(this);


        visitsView.setLayoutManager(new LinearLayoutManager(this));
        visitsView.setAdapter(visitAdapter);


        Runnable filterDates=new Runnable() {
            @Override
            public void run() {
                if(visitAdapter!=null)
                {
                    visitAdapter.filter(fromDateUtils.getMinDate(),toDateUtils.getMaxDate());
                }

            }
        };

        fromDateUtils.onchange=filterDates;
        toDateUtils.onchange=filterDates;

    }

    private List<RecognitionResult> allVisits;


    private void feedData(String subjectid) {
        SubjectDao subjectDao= MyDatabase.getInstance(this).subjectDao();
            CompletableFuture.supplyAsync(()->{
                try{
                    return subjectDao.getSubjectByuid(subjectid);
                }catch (Exception es)
                {
                    es.printStackTrace();
                    return null;
                }
            }).thenAcceptAsync(subject->{
                if(subject!=null)
                {
                    TextView name=findViewById(R.id.profile_name);
                    TextView watchlist=findViewById(R.id.profile_watchlist);
                    ImageView currentProfile=findViewById(R.id.current_profile);
                    name.setText(subject.getName());
                    watchlist.setText("Watchlist : "+subject.getWatchlist());
                    currentProfile.setImageBitmap(subject.loadImage(this));
                    visitAdapter.setSubject(subject);
                }
            }, AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void onResume() {
        super.onResume();
        Intent verificationIntent=getIntent();
        if(verificationIntent.hasExtra("subject"))
        {
            feedData(verificationIntent.getStringExtra("subject"));
        }
    }
}