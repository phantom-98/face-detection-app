package com.facecoolalert.ui.subject.reports;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.EnrollmentReportDao;

import java.util.List;

public class EnrollmentsPreview extends Dialog {

    private Activity activity;
    private RecyclerView recyclerView;

    private EnrollmentReportDao enrollmentReportDao;

    public EnrollmentsPreview(Context context, Activity activity) {
        super(context);
        this.activity=activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_enrollments);

        this.recyclerView=findViewById(R.id.recycleView);

        Button closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(); // Close the dialog
            }
        });

        this.enrollmentReportDao= MyDatabase.getInstance(getContext()).enrollmentReportDao();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new Thread(()->{

            List<Long> all=enrollmentReportDao.listDates();
            new Handler(Looper.getMainLooper()).post(()->{
                recyclerView.setAdapter(new EnrollmentReportsAdapter(getContext(),all,enrollmentReportDao,activity,this));
            });


        }).start();



    }

}
