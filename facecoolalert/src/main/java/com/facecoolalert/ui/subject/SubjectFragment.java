package com.facecoolalert.ui.subject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.databinding.FragmentSubjectListBinding;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.ui.base.BaseFragment;

import com.facecoolalert.ui.subject.enrollments.EnrollSubject;
import com.facecoolalert.ui.subject.multiEnrollment.FolderPicker.EnrollFolder;
import com.facecoolalert.ui.subject.reports.EnrollmentsPreview;
import com.facecoolalert.utils.RecognitionUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A fragment representing a list of Items.
 */
public class SubjectFragment extends BaseFragment {

    public static int SUBJECTCARD_REQUEST_CODE = 105;
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private EditText search;

    private Button viewReports;
    private TextView counterText;
    private RecyclerView recyclerView;
    private SubjectRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SubjectFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SubjectFragment newInstance(int columnCount) {
        SubjectFragment fragment = new SubjectFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public ViewDataBinding getBinding(LayoutInflater inflater) {
        mViewDataBinding=FragmentSubjectListBinding.inflate(inflater);
        return mViewDataBinding;
    }

    @Override
    public void onPostCreateView() {

        View view=mViewDataBinding.getRoot();
        this.search=view.findViewById(R.id.search_subjects);
        this.viewReports=view.findViewById(R.id.viewReports);
        this.counterText=view.findViewById(R.id.counter);
        this.recyclerView = view.findViewById(R.id.subjects_list);



        FloatingActionButton action=view.findViewById(R.id.button_add_new_subject);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopUp(v);
            }
        });



        mToolBarLeftIcon.setOnClickListener(v -> {
            openMenu();
        });

        feedSubjectList(view);


        this.viewReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewEnrollments();
            }
        });

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }


    private void openPopUp(View view) {
        // Create the custom dialog
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.enrollment_method);

// Set dialog properties
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

// Find views within the dialog
        TextView dialogHeading = dialog.findViewById(R.id.dialog_heading);
        Button buttonFolderEnrollment = dialog.findViewById(R.id.buttonFolderEnrollment);
        Button buttonSingleEnrollment = dialog.findViewById(R.id.buttonSingleEnrollment);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);

// Set button click listeners
        buttonFolderEnrollment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Folder Enrollment button click

                dialog.dismiss();
                Intent enrollIntent=new Intent(getContext(), EnrollFolder.class);

                //startActivity(enrollIntent);
                getActivity().startActivityForResult(enrollIntent,SubjectFragment.SUBJECTCARD_REQUEST_CODE);
            }
        });

        buttonSingleEnrollment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Single Enrollment button click


                dialog.dismiss();
                Intent enrollIntent=new Intent(getContext(), EnrollSubject.class);

                //startActivity(enrollIntent);
                getActivity().startActivityForResult(enrollIntent,SubjectFragment.SUBJECTCARD_REQUEST_CODE);
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Cancel button click

                dialog.dismiss();
            }
        });

// Show the dialog
        dialog.show();

    }



    private void feedSubjectList(View view) {

        RecognitionUtils.refreshSubjects();
        SubjectDao subjectDao= MyDatabase.getInstance(getContext()).subjectDao();


        // Determine the current device orientation
        int orientation = getResources().getConfiguration().orientation;

        // Set up the RecyclerView with the appropriate number of columns
        int numberOfColumns;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            numberOfColumns = 2;
        } else {
            numberOfColumns = 1;
        }
       
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),numberOfColumns));

        CompletableFuture.supplyAsync(()->{
                    try {
                        return subjectDao.getAllSubjects();
                    }catch (Exception es) {
                        es.printStackTrace();
                        return Collections.emptyList();
                    }
                    }).thenAcceptAsync(subjects->{
                            this.adapter = new SubjectRecyclerViewAdapter((List<Subject>) subjects, getContext());
                            new Handler(Looper.getMainLooper()).post(()->{
                                recyclerView.setAdapter(adapter);
                                counterText.setText(subjects.size() + " Subjects Enrolled");
                            });

                            search.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    adapter.filter(s.toString());
                                }
                                @Override
                                public void afterTextChanged(Editable s) {}
                            });

                        }, AsyncTask.THREAD_POOL_EXECUTOR );
        
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("Was Reached");
        if (requestCode ==  SUBJECTCARD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println("Updating");
                feedSubjectList(mViewDataBinding.getRoot());
            }
        }
    }


    private void viewEnrollments() {

        try {
            new EnrollmentsPreview(getContext(),getActivity()).show();
        }catch (Exception ytttr)
        {
            ytttr.printStackTrace();
        }
    }

}