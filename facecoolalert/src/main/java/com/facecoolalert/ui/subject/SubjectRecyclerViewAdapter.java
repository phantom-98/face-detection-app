package com.facecoolalert.ui.subject;

import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facecoolalert.R;
import com.facecoolalert.common.PromptDialog;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.subject.enrollments.SubjectCard;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SubjectRecyclerViewAdapter extends RecyclerView.Adapter<SubjectRecyclerViewAdapter.ViewHolder> {

    private final List<Subject> mValues;

    private List<Subject> filteredValues;
    private Context context;

    private ExecutorService threadPool= Executors.newSingleThreadExecutor();

    public SubjectRecyclerViewAdapter(List<Subject> items, Context context) {
        mValues = items;
        filteredValues=new ArrayList<>(mValues);
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Subject currentSubject=filteredValues.get(position);
        holder.nameTextView.setText(currentSubject.getName());
        holder.profile.setImageBitmap(null);
        threadPool.submit(()->{
            final Bitmap fetched=currentSubject.loadImage(context);
            new Handler(Looper.getMainLooper()).post(()->{
                holder.profile.setImageBitmap(fetched);
            });
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, SubjectCard.class);
                intent.putExtra("subject",currentSubject.getUid());

                ((Activity)v.getContext()).startActivityForResult(intent,SubjectFragment.SUBJECTCARD_REQUEST_CODE);

            }
        });
        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PromptDialog promptDialog=new PromptDialog(context);
                promptDialog.setText("Are you Sure You want to delete subject : "+currentSubject.getName()+" ? ");
                promptDialog.setOnAcceptListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        System.out.println("deleting");
                        SubjectDao subjectDao= MyDatabase.getInstance(context).subjectDao();
                        new Thread()
                        {
                            public void run()
                            {
                                try{
                                    currentSubject.deleteImage(context.getApplicationContext());
                                    subjectDao.delete(currentSubject);
                                    new android.os.Handler(Looper.getMainLooper()).post(()->{
                                    Toast.makeText(v.getContext(), "Deleted SuccessFully", Toast.LENGTH_SHORT).show();
                                    ((MainActivity)context).setSubjectsFragment();
                                    });
                                }catch (Exception ed)
                                {
                                    ed.printStackTrace();
                                }
                            }
                        }.start();
                        promptDialog.dismiss();
                    }
                });
                promptDialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return filteredValues.size();
    }

    //start of search and filter

    // Add a method to filter the data based on search query
    public void filter(String query) {
        query = query.toLowerCase(Locale.getDefault());
        filteredValues.clear();
        if (query.length() == 0) {
            filteredValues.addAll(mValues); // If the query is empty, show all subjects
        } else {
            for (Subject subject : mValues) {
                if (subject.getName().toLowerCase(Locale.getDefault()).contains(query)||(subject.getID()!=null&&subject.getID().contains(query)))
                {
                    filteredValues.add(subject);
                }
            }
        }
        notifyDataSetChanged(); // Notify the adapter of data change
    }

    // Optionally, add a method to clear the filter
    public void clearFilter() {
        filteredValues.clear();
        filteredValues.addAll(mValues); // Restore the original data
        notifyDataSetChanged(); // Notify the adapter of data change
    }


    //end of search and filter



    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageView profile;
        public ImageButton btn;


        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.subject_name);
            // Initialize other views and event handlers here
            profile=itemView.findViewById(R.id.subject_image);

            btn=itemView.findViewById(R.id.btn_remove_subject);
        }
    }
}