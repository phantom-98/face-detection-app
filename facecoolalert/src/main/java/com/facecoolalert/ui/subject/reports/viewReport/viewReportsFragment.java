package com.facecoolalert.ui.subject.reports.viewReport;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.EnrollmentReportDao;
import com.facecoolalert.database.entities.EnrollmentReport;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.subject.multiEnrollment.FolderPicker.AddFolderViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class viewReportsFragment extends Fragment {







    private String folderPath;
    private AddFolderViewModel mViewModel;
    private RecyclerView recyclerView;
    private int progres;
    private int tot;
    private int enrolled;
    private int error;

    private Long enrollmentDate;

    private static Long prevEnrollmentDate=0L;

    private EnrollmentReportDao enrollmentReportDao;

    public viewReportsFragment(Long enrollmentDate,EnrollmentReportDao enrollmentReportDao)
    {
        this.enrollmentDate=enrollmentDate;
        this.enrollmentReportDao=enrollmentReportDao;
        prevEnrollmentDate=enrollmentDate;
    }

    public static viewReportsFragment newInstance(Long enrollmentDate,EnrollmentReportDao enrollmentReportDao) {
        return new viewReportsFragment(enrollmentDate,enrollmentReportDao);
    }

    public viewReportsFragment()
    {
        enrollmentReportDao= MyDatabase.getInstance(getContext()).enrollmentReportDao();
        this.enrollmentDate= prevEnrollmentDate;

    }

    public viewReportsFragment(Bundle args)
    {
        setArguments(args);
    }

    public static viewReportsFragment newInstance() {
        viewReportsFragment fragment = new viewReportsFragment();

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View myView=inflater.inflate(R.layout.dialog_enrollment_reports, container, false);

        myView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    ((MainActivity)getActivity()).removeFragment(viewReportsFragment.class);

                }catch (Exception ed)
                {
                    ed.printStackTrace();
                }
            }
        });

//        myView.findViewById(R.id.check_follow).setVisibility(View.GONE);




        this.recyclerView=myView.findViewById(R.id.rv_enroll_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        ReportPhotosAdapter folderPhotosAdapter=new ReportPhotosAdapter();
        recyclerView.setAdapter(folderPhotosAdapter);

        recyclerView.getRootView().findViewById(R.id.tv_info_treashold).setVisibility(View.INVISIBLE);
        return myView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddFolderViewModel.class);
        // TODO: Use the ViewModel
        mViewModel.setFolderPath(folderPath);


        new Thread(()->{

            List<EnrollmentReport> all=enrollmentReportDao.getAll(enrollmentDate);
            this.progres=all.size();
            this.tot=all.size();


            List<File> items=new ArrayList<>();
            List<String> sitems=new ArrayList<>();
            this.enrolled=0;

            this.error=0;

            for(int i=0;i<all.size();i++)
            {
                EnrollmentReport r=all.get(i);

                sitems.add(r.getImageFile());
                if(r.getStatus().startsWith("success"))
                {
                    enrolled++;
                    ((ReportPhotosAdapter)recyclerView.getAdapter()).holderstatus.put(i,r.getStatus());
                }
                else{
                    error++;
                    ((ReportPhotosAdapter)recyclerView.getAdapter()).holderstatus.put(i,r.getStatus());
                }

            }
            ((ReportPhotosAdapter)recyclerView.getAdapter()).setItems(items);

            String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
            if(all.size()>0)
            {
                try {


                    File first = new File(all.get(0).getImageFile());
                    File parentFile=first.getParentFile();
                    if(parentFile.exists()) {
                        for (File f : parentFile.listFiles()) {
                            String filePath = f.getAbsolutePath();
                            if (!sitems.contains(filePath))
                                for (String j : imageExtensions)
                                    if (filePath.toLowerCase().endsWith(j)) {
                                        sitems.add(filePath);
                                        break;
                                    }
                        }
                    }
                    else
                        Toast.makeText(getContext(), "Enrollment Folder is not On this Device : "+parentFile.getPath(), Toast.LENGTH_LONG).show();
                }catch (Exception es)
                {
                    es.printStackTrace();
                }
            }
//            Log.d("Enrollment Report","SItems : "+sitems);

            for(String i:sitems)
                items.add(new File(i));

            sitems.clear();
            this.tot=items.size();





            new Handler(Looper.getMainLooper()).post(()->{
            ReportPhotosAdapter folderPhotosAdapter=(ReportPhotosAdapter)recyclerView.getAdapter();
            folderPhotosAdapter.setItems(items);

            folderPhotosAdapter.notifyDataSetChanged();



            final TextView infotext=recyclerView.getRootView().findViewById(R.id.info_text);
            updateStats(infotext);
            Button start=recyclerView.getRootView().findViewById(R.id.btn_start);
//            start.setVisibility(View.GONE);
            });


        }).start();

    }

    private int pos=0;




    private void updateStats(TextView infotext) {
        String str="Progress: "+progres+"/"+tot+" ; "+enrolled+" enrolled ; "+error+" error(s)";
        infotext.setText(str);
    }

    private void placeError(ReportPhotosAdapter.ViewHolder currentHolder, String str) {
        currentHolder.progressBar.setVisibility(View.GONE);
        currentHolder.card.setBackgroundColor(Color.RED);
        currentHolder.text.setText(str);
    }

    private void placeSuccess(ReportPhotosAdapter.ViewHolder currentHolder) {
        currentHolder.progressBar.setVisibility(View.GONE);
        currentHolder.card.setBackgroundColor(Color.GREEN);
        //currentHolder.text.setText(str);
    }



}