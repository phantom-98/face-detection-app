package com.facecoolalert.ui.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.facecoolalert.R;
import com.facecoolalert.common.PromptDialog;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.AlertLogDao;
import com.facecoolalert.database.Repositories.EnrollmentReportDao;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.databinding.FragmentAdditionalSettingsBinding;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.settings.appdatabaseimportexport.AppDatabaseFragment;
import com.facecoolalert.ui.settings.history.HistoryFragment;
import com.facecoolalert.ui.settings.logs.LogsFragment;
import com.facecoolalert.ui.settings.resourcemonitor.ResourcesMonitorFragment;
import com.facecoolalert.utils.RecognitionUtils;

public class AdditionalSettingsFragment extends Fragment {

    private Button deleteSubjects;

    private Button resourcesButton;

    private Button logsButton;

    private Button historyButton;

    private Button appDatabase;

    private Button advancedSettingsButton;

    private Button deleteFaceLog;

    private long startTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentAdditionalSettingsBinding binding=FragmentAdditionalSettingsBinding.inflate(inflater,container,false);

        binding.backButton.setOnClickListener((v)->{
            ((MainActivity)(getActivity())).removeFragment(AdditionalSettingsFragment.class);
        });

        deleteSubjects=binding.deleteSubjects;
        resourcesButton=binding.resources;
        logsButton=binding.logs;
        historyButton=binding.historyImportExport;
        appDatabase=binding.fullDatabaseImportExport;
        advancedSettingsButton=binding.advancedSettingsButton;
        deleteFaceLog=binding.deleteFaceLog;
        configureListeners();
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configureListeners() {

        handleDeleteSubjects();

        advancedSettingsButton.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startTime = System.currentTimeMillis();
                    return true;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    if((now - startTime) >= 5000) {
                        navigateToAdvancedSettings();
                    }
                    v.performClick();
                    return true;
            }
            return false;
        });

        resourcesButton.setOnClickListener((v -> {
            ((MainActivity)getActivity()).addFragment(new ResourcesMonitorFragment());
        }));


        logsButton.setOnClickListener((v -> {
            ((MainActivity)getActivity()).addFragment(new LogsFragment());
        }));

        historyButton.setOnClickListener((v -> {
            ((MainActivity)getActivity()).addFragment(new HistoryFragment());
        }));

        appDatabase.setOnClickListener(v->{
            ((MainActivity)getActivity()).addFragment(AppDatabaseFragment.getInstance());
        });

        deleteFaceLog.setOnClickListener(view -> {
            PromptDialog promptDialog=new PromptDialog(getContext());

            promptDialog.setText("Are you sure you want to Delete all Faces Log and ALerts Log ? This cannot be undone");
            promptDialog.setOnAcceptListener(v -> {
                RecognitionResultDao recognitionResultDao=MyDatabase.getInstance(getContext()).recognitionResultDao();
                AlertLogDao alertLogDao=MyDatabase.getInstance(getContext()).alertLogDao();
                new Thread(()->{
                    try {
                        recognitionResultDao.deleteAll();
                        alertLogDao.deleteAll();
                    }catch (Exception esd)
                    {
                        esd.printStackTrace();
                    }
                }).start();
                Toast.makeText(getContext(), "FaceLog Deleted Successfully", Toast.LENGTH_SHORT).show();
                promptDialog.dismiss();
            });

            promptDialog.show();
        });
    }

    private void navigateToAdvancedSettings() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_main, new AdvancedSettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void handleDeleteSubjects() {
        deleteSubjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PromptDialog promptDialog=new PromptDialog(getContext());

                promptDialog.setText("Are you sure you want to Delete all subjects ? This cannot be undone");
                promptDialog.setOnAcceptListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SubjectDao subjectDao= MyDatabase.getInstance(getContext()).subjectDao();
                        EnrollmentReportDao enrollmentReportDao=MyDatabase.getInstance(getContext()).enrollmentReportDao();
                        new Thread(()->{
                            try {

                                for(Subject i: RecognitionUtils.subjects)
                                    i.deleteImage(getContext());

                                subjectDao.deleteAll();
                                enrollmentReportDao.deleteAll();
                                RecognitionUtils.refreshSubjects();
                            }catch (Exception esd)
                            {
                                esd.printStackTrace();
                            }

                        }).start();
                        RecognitionUtils.subjects.clear();
                        RecognitionUtils.refreshSubjects();
                        Toast.makeText(getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        promptDialog.dismiss();

                    }
                });

                promptDialog.show();


            }
        });
    }
}
