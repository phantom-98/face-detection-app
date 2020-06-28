package com.facecoolalert.ui.watchlist.addWatchList;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.databinding.ViewDataBinding;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.facecoolalert.R;
import com.facecoolalert.common.PromptDialog;
import com.facecoolalert.common.subjectsListing.CheckableSubjectListViewAdapter;
import com.facecoolalert.database.Repositories.AlertDao;
import com.facecoolalert.database.entities.WatchList;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.databinding.FragmentAddWatchlistBinding;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.base.BaseFragment;
import com.facecoolalert.utils.DynamicResultsQueryGenerator;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AddWatchList extends BaseFragment {
    private static WatchList existing;
    private View view;

    private EditText nameEditText;
    private EditText typeEditText;
    private EditText noteEditText;
    private EditText searchSubjectsEditText;
    private CheckBox assignAllCheckBox;
    private ListView listView;
    private Button cancelButton;
    private Button saveButton;
    private WatchlistDao watchlistDao;
    private LayoutInflater layoutInflator;

    public AddWatchList(WatchList watchList) {
        super();
        existing = watchList;
    }

    public AddWatchList()
    {

    }

    @Override
    public ViewDataBinding getBinding(LayoutInflater inflater) {
        this.mViewDataBinding= FragmentAddWatchlistBinding.inflate(inflater);
        this.layoutInflator=inflater;
        return mViewDataBinding;
    }

    @Override
    public void onPostCreateView() {


        mToolBarLeftIcon.setOnClickListener(v -> {
            openMenu();
        });

        this.view=mViewDataBinding.getRoot();

        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity act= (MainActivity) getActivity();
                act.removeFragment(AddWatchList.class);
            }
        });

        initializeIds();

        if (existing != null) {
            nameEditText.setText(existing.getName());
            typeEditText.setText(existing.getType());
            noteEditText.setText(existing.getNote());

            View deleteIcon=view.findViewById(R.id.deleteIcon);
            deleteIcon.setVisibility(View.VISIBLE);
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PromptDialog promptDialog=new PromptDialog(getContext());
                    promptDialog.setText("Are you Sure You want to delete this WatchList "+" ? ");
                    promptDialog.setOnAcceptListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            System.out.println("deleting");
                            new Thread()
                            {
                                public void run()
                                {
                                    try{
                                        watchlistDao.delete(existing);
                                        try {
                                            subjectDao.updateWatchList(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getWatchListUpdater("Default", " watchlist='"+ existing.getName()+"'")));

                                        }catch (Exception subje)
                                        {
                                            subje.printStackTrace();
                                        }
                                        AlertDao alertDao=MyDatabase.getInstance(getContext()).alertDao();
                                        alertDao.deleteByWatchList(existing.getUid());
                                        new android.os.Handler(Looper.getMainLooper()).post(()->{
                                            Toast.makeText(v.getContext(), "Deleted SuccessFully", Toast.LENGTH_SHORT).show();
                                            ((MainActivity)getActivity()).setWatchListFragment();
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

        if(existing ==null)
            handleSaving();
        else {
            ((TextView)view.findViewById(R.id.title)).setText("Edit Watchlist");
            handleUpdating();
        }

        feedStudents();
    }



    private void initializeIds() {
        nameEditText = view.findViewById(R.id.name);
        typeEditText = view.findViewById(R.id.type);
        noteEditText = view.findViewById(R.id.note);
        searchSubjectsEditText = view.findViewById(R.id.search_subjects);
        assignAllCheckBox = view.findViewById(R.id.assignall);
        listView = view.findViewById(R.id.recycleView);
        cancelButton = view.findViewById(R.id.cancel_button);
        saveButton = view.findViewById(R.id.submitbutton);
    }
    private CheckableSubjectListViewAdapter studentsAdapter;

    private SubjectDao subjectDao;
    private void feedStudents() {
        this.subjectDao = MyDatabase.getInstance(getContext()).subjectDao();
        CompletableFuture.supplyAsync(()->{
            try {
                return subjectDao.listSubjects();
            }catch (Exception es)
            {
                es.printStackTrace();
                return Collections.emptyList();
            }
        }).thenAcceptAsync(subjects -> {
            new Handler(Looper.getMainLooper()).post(()->{
                studentsAdapter = new CheckableSubjectListViewAdapter(getContext(), (List<Subject>) subjects, existing, layoutInflator);
                listView.setAdapter(studentsAdapter);
            });
        }, AsyncTask.THREAD_POOL_EXECUTOR);


        searchSubjectsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (studentsAdapter != null) {
                    // Use runOnUiThread to update the UI
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            studentsAdapter.filter(s.toString());
                        }
                    });
                }

                ((NestedScrollView)view.findViewById(R.id.scroller)).smoothScrollTo(0, 300);


            }

            @Override
            public void afterTextChanged(Editable s) {



            }
        });

        assignAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (studentsAdapter != null) {
                        // Use runOnUiThread to update the UI
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                studentsAdapter.checkAll();
                            }
                        });
                    }
                } else {
                    if (studentsAdapter != null) {
                        // Use runOnUiThread to update the UI
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                studentsAdapter.uncheckAll();
                            }
                        });
                    }
                }
            }
        });


    }



    private void handleSaving() {
        this.watchlistDao=MyDatabase.getInstance(getContext()).watchlistDao();

        Handler uiHandler=new Handler();
        final MainActivity act= (MainActivity) getActivity();
        view.findViewById(R.id.submitbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nameEditText.getText().toString().trim().isEmpty()) {
                    nameEditText.setError("Cannot be Empty");
                    return;
                }


                new Thread(()->{
                    try{
                        String watchlistName=nameEditText.getText().toString().trim();
                        if(watchlistDao.watchlists(watchlistName)>0)
                        {
                            uiHandler.post(()->{
                            nameEditText.setError("Watchlist Already Exists");
                            });
                            return;
                        }

                        String type=typeEditText.getText().toString().trim();
                        String note=noteEditText.getText().toString();

                        WatchList watchList=new WatchList();
                        watchList.setName(watchlistName);
                        watchList.setType(type);
                        watchList.setNote(note);
                        watchList.setCreatedOn(new Date().getTime());

                        watchlistDao.insertWatchlist(watchList);

                        SubjectDao subjectDao=MyDatabase.getInstance(getContext()).subjectDao();
                        try {
                            subjectDao.updateWatchList(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getWatchListUpdater(watchlistName, " num in(select num from Subject where (firstName || ' ' || lastName) in ('" + String.join("','", studentsAdapter.getCheckedItems()) + "'))")));

                        }catch (Exception subje)
                        {
                            subje.printStackTrace();
                        }
                        uiHandler.post(()->{

                            Toast.makeText(getContext(), "Added Successfully", Toast.LENGTH_SHORT).show();
                            //act.removeFragment(AddWatchList.class);
                            act.setWatchListFragment();

                        });




                    }catch (Exception esd)
                    {
                        esd.printStackTrace();
                    }
                }).start();
            }
        });
    }


    private void handleUpdating() {
        this.watchlistDao=MyDatabase.getInstance(getContext()).watchlistDao();

        Handler uiHandler=new Handler();
        final MainActivity act= (MainActivity) getActivity();
        view.findViewById(R.id.submitbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nameEditText.getText().toString().trim().isEmpty()) {
                    nameEditText.setError("Cannot be Empty");
                    return;
                }


                new Thread(()->{
                    try{
                        String watchlistName=nameEditText.getText().toString().trim();


                        if(!existing.getName().equals(watchlistName)&&watchlistDao.watchlists(watchlistName)>0)
                        {
                            uiHandler.post(()->{
                                nameEditText.setError("Watchlist Already Exists");
                            });
                            return;
                        }

                        String type=typeEditText.getText().toString().trim();
                        String note=noteEditText.getText().toString();

                        WatchList watchList= existing;
                        watchList.setName(watchlistName);
                        watchList.setType(type);
                        watchList.setNote(note);


                        watchlistDao.update(watchList);

                        SubjectDao subjectDao=MyDatabase.getInstance(getContext()).subjectDao();
                        try {
                            subjectDao.updateWatchList(new SimpleSQLiteQuery(DynamicResultsQueryGenerator.getWatchListUpdater(watchlistName, " uid in(select uid from Subject where (firstName || ' ' || lastName) in ('" + String.join("','", studentsAdapter.getCheckedItems()) + "'))")));

                        }catch (Exception subje)
                        {
                            subje.printStackTrace();
                        }
                        uiHandler.post(()->{

                            Toast.makeText(getContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                            //act.removeFragment(AddWatchList.class);
                            act.setWatchListFragment();

                        });




                    }catch (Exception esd)
                    {
                        esd.printStackTrace();
                    }
                }).start();
            }
        });
    }
}
