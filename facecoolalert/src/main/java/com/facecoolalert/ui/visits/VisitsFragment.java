package com.facecoolalert.ui.visits;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.facecoolalert.R;
import com.facecoolalert.common.searchView.SearchableView;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.databinding.FragmentVisitsBinding;
import com.facecoolalert.ui.base.BaseFragment;
import com.facecoolalert.utils.datetimepicker.DatePickerUtils;
import com.facecoolalert.utils.datetimepicker.TimePickerUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VisitsFragment extends BaseFragment {

    private Button filterButton;
    private RecognitionResultDao recognitionResultDao;

    private SubjectDao subjectDao;

    private GenVisitAdapter genVisitAdapter;

    private RecyclerView visitsRecycleView;
//    private View popupView;
//    private PopupWindow popupWindow;
    private SearchableView selectId;
    private SearchableView selectName;
    private SearchableView selectLocation;
    private DatePickerUtils fromDatePicker;
    private DatePickerUtils toDatePicker;
    private TimePickerUtils fromTimePicker;
    private TimePickerUtils toTimePicker;
    private Spinner timeType;
    private LinearLayout customTimeView;
    private View applyButton;
    private Spinner showIdentities;

    @Override
    public ViewDataBinding getBinding(LayoutInflater inflater) {
        mViewDataBinding=FragmentVisitsBinding.inflate(inflater);
        return mViewDataBinding;
    }

    @Override
    public void onPostCreateView() {

        filterButton=mViewDataBinding.getRoot().findViewById(R.id.filter_container);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterMenu();
            }
        });
        mToolBarLeftIcon.setOnClickListener(v -> {
            genVisitAdapter.stop();
            openMenu();
        });

        this.recognitionResultDao= MyDatabase.getInstance(getContext()).recognitionResultDao();
        this.subjectDao=MyDatabase.getInstance(getContext()).subjectDao();
        this.visitsRecycleView=mViewDataBinding.getRoot().findViewById(R.id.visit_logs);

        // Determine the current device orientation
        int orientation = getResources().getConfiguration().orientation;

        // Set up the RecyclerView with the appropriate uidber of columns
        int uidberOfColumns;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            uidberOfColumns = 2;
        } else {
            uidberOfColumns = 1;
        }

        visitsRecycleView.setLayoutManager(new GridLayoutManager(getContext(),uidberOfColumns));


        genVisitAdapter = new GenVisitAdapter(this.getContext(),visitsRecycleView,this);
//        genVisitAdapter.recyclerView=visitsRecycleView;
        visitsRecycleView.setAdapter(genVisitAdapter);

        feedLogs(mViewDataBinding.getRoot());
        initFilterMenu();


        feedFilterData();

    }




    private void feedLogs(View root) {

        visitsRecycleView.setVerticalScrollBarEnabled(true);
        genVisitAdapter.setMaxDate(new Date().getTime());

    }

    private Boolean showing=false;

    private Dialog filterDialog;

    public void initFilterMenu() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Using a Dialog instead of a PopupWindow
        filterDialog = new Dialog(getActivity());
        filterDialog.setContentView(R.layout.filter_popup);
        filterDialog.getWindow().setLayout((int) ((screenWidth * 0.75) / 1), ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        WindowManager.LayoutParams layoutParams = filterDialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        layoutParams.x = 0; // Set the X coordinate

        layoutParams.y =250; // Set the Y coordinate
        filterDialog.getWindow().setAttributes(layoutParams);


        filterDialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterMenu();
            }
        });

        configureActions();
    }

    public void showFilterMenu() {
        try {
            showing = !showing;

            if (showing) {
                filterDialog.show();
            } else if (filterDialog != null && filterDialog.isShowing()) {
                filterDialog.dismiss();
            }
        } catch (Exception er) {
            er.printStackTrace();
        }
    }


    private void feedFilterData() {

        this.selectId=filterDialog.findViewById(R.id.select_id);
        this.selectName=filterDialog.findViewById(R.id.select_name);
        this.selectLocation=filterDialog.findViewById(R.id.select_location);

        selectId.setTitle("Select Id");
        selectName.setTitle("Select Name");
        selectLocation.setTitle("Select Location");

        LiveData<List<String>> subjectIdsObserver = subjectDao.listIds();
        subjectIdsObserver.observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                selectId.setArrayList(new ArrayList<>(strings));
                subjectIdsObserver.removeObserver(this);
            }
        });

        LiveData<List<String>> subjectsObserver = subjectDao.listNames();
        subjectsObserver.observe(this, new Observer<List<String>>() {
                    @Override
                    public void onChanged(List<String> strings) {
                        selectName.setArrayList(new ArrayList<>(strings));
                        subjectsObserver.removeObserver(this);
                    }
                });

        LiveData<List<String>> locationsObserver = recognitionResultDao.listLocations();
        locationsObserver.observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                selectLocation.setArrayList(new ArrayList<>(strings));
                locationsObserver.removeObserver(this);
            }
        });


        this.fromDatePicker=new DatePickerUtils(filterDialog.findViewById(R.id.fromdate));
        this.toDatePicker=new DatePickerUtils(filterDialog.findViewById(R.id.todate));

        this.fromTimePicker=new TimePickerUtils(filterDialog.findViewById(R.id.fromtime));
        this.toTimePicker=new TimePickerUtils(filterDialog.findViewById(R.id.totime));
    }


    private void configureActions() {


        this.timeType= filterDialog.findViewById(R.id.timeType);
        showCorrectViews();
        timeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showCorrectViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        this.applyButton=filterDialog.findViewById(R.id.apply);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateFilters();
                }catch (Exception err)
                {
                    err.printStackTrace();
                }
            }
        });


    }



    private void showCorrectViews() {
        this.customTimeView=((LinearLayout)filterDialog.findViewById(R.id.custom_time_form));
        if(timeType.getSelectedItem().toString().toLowerCase().equals("custom"))
            customTimeView.setVisibility(View.VISIBLE);
        else
            customTimeView.setVisibility(View.GONE);

        this.showIdentities=(Spinner)filterDialog.findViewById(R.id.show);
    }


    private void updateFilters() {
        applyButton.setEnabled(false);

        Long maxDate=new Date().getTime();

        String query="date<="+maxDate+" and ";
        List<String> ids=selectId.getChecked();
        if(ids.size()>0)
        {
            query += "subjectid IN (select uid from Subject where ID in ('" + String.join("','", ids) + "')) AND ";

        }

        List<String> locations=selectLocation.getChecked();
        if(locations.size()>0)
            query += "location IN ('" + String.join("','", locations) + "') AND ";

        List<String> names=selectName.getChecked();
        if(names.size()>0)
            query += "subjectid IN (select uid from Subject where (firstName || ' ' || lastName) in ('" + String.join("','", names) + "')) AND ";


        if(showIdentities.getSelectedItem().toString().equalsIgnoreCase("identified"))
            query+="subjectid is not null and subjectid!='' and ";
        else if(showIdentities.getSelectedItem().toString().equalsIgnoreCase("unidentified"))
            query+="subjectid is null and ";


        if(timeType.getSelectedItem().toString().equalsIgnoreCase("custom"))
        {
            Long from=fromDatePicker.getCombinedTime(fromTimePicker.getCalendar()).getTime();
            Long to=toDatePicker.getCombinedTime(toTimePicker.getCalendar()).getTime();

            if(to-from>1000)
                query+=" date >= "+from+" and date <= "+to+" and ";
        }
        else{
            Long from = new Date().getTime();
            Long minuteml = 1000L * 60;

            if (timeType.getSelectedItem().toString().equalsIgnoreCase("15 Minutes")) {
                from = from - minuteml * 15;
            } else if (timeType.getSelectedItem().toString().equalsIgnoreCase("1 Hour")) {
                from =from - minuteml * 60;
            } else if (timeType.getSelectedItem().toString().equalsIgnoreCase("1 Day")) {
                from = from- minuteml * 60 * 24;
            }
            else{
                System.out.println("Wrong selection : "+timeType.getSelectedItem().toString());
            }

            query += " date >= " + from + " and ";

        }

        genVisitAdapter.setQuery(query);
//        showfilterMenu();
        showFilterMenu();
        applyButton.setEnabled(true);

    }


}
