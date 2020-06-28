package com.facecoolalert.ui.subject.reports;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.Repositories.EnrollmentReportDao;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.subject.reports.viewReport.viewReportsFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnrollmentReportsAdapter extends RecyclerView.Adapter<EnrollmentReportsAdapter.EnrollmentViewHolder> {
    private Dialog dd;
    private Activity activity;
    private EnrollmentReportDao enrollmentReportDao;
    private Context context;
    private List<Long> all;

    private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("EEE, d MMM yyyy 'at' HH:mm:ss", Locale.ENGLISH);

    public EnrollmentReportsAdapter(Context context, List<Long> all, EnrollmentReportDao enrollmentReportDao, Activity activity, Dialog dd) {
        this.all=all;
        this.context=context;
        this.enrollmentReportDao=enrollmentReportDao;
        this.activity=activity;
        this.dd=dd;

    }

    @NonNull
    @Override
    public EnrollmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_enrollment_reports_listing, parent, false);
        return new EnrollmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnrollmentViewHolder holder, int position) {
        Long curr=all.get(position);
        holder.nameTextView.setText("Enrollment on "+simpleDateFormat.format(new Date(curr)));
        new Thread(()->{
            int count= enrollmentReportDao.countSuccess(curr);
            new Handler(Looper.getMainLooper()).post(()->{
                holder.counterTextView.setText("("+count+")");
                holder.counterTextView.setTextColor(Color.RED);
            });
        }).start();

        holder.itemView.setOnClickListener(v -> {
            try{
                dd.dismiss();
                ((MainActivity)activity).addFragment(new viewReportsFragment(curr,enrollmentReportDao));
            }catch (Exception ed)
            {
                ed.printStackTrace();
            }
        });




    }

    @Override
    public int getItemCount() {
        return all.size();
    }


    public class EnrollmentViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public  TextView counterTextView;

        public EnrollmentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            counterTextView = itemView.findViewById(R.id.counter);
        }

    }
}
