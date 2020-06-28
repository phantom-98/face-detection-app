package com.facecoolalert.ui.Alerts.Alert;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.facecool.cameramanager.entity.CameraInfo;
import com.facecoolalert.App;
import com.facecoolalert.R;
import com.facecoolalert.common.PromptDialog;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.AlertDao;
import com.facecoolalert.database.Repositories.DistributionListDao;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.database.entities.Alert;
import com.facecoolalert.ui.Alerts.AlertFragment;
import com.facecoolalert.ui.MainActivity;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddAlertFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddAlertFragment extends Fragment {


    private AlertFragment alertFragment;
    private static Alert existing;
    private View backButton;


    private Button cancelButton;
    private Button submitButton;


    private TextView titleTextView;
    private EditText nameEditText;
    private Spinner watchlistSpinner;
    private Spinner distributionListSpinner;
    private Spinner locationSpinner;


    private WatchlistDao watchlistDao;

    private DistributionListDao distributionListDao;






    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    

    public AddAlertFragment() {
        // Required empty public constructor
        watchlistDao= MyDatabase.getInstance(getContext()).watchlistDao();

        distributionListDao=MyDatabase.getInstance(getContext()).distributionListDao();
    }

    public AddAlertFragment(AlertFragment alertFragment, Alert alert) {
        existing=alert;
        this.alertFragment=alertFragment;
        

        watchlistDao= MyDatabase.getInstance(getContext()).watchlistDao();

        distributionListDao=MyDatabase.getInstance(getContext()).distributionListDao();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddAlertFragment newInstance(String param1, String param2) {
        AddAlertFragment fragment = new AddAlertFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_add_alert, container, false);


        assignAllIds(view);
        setNavigationButtons(view);

        setSources();


        handleSaving();

        return view;
    }

    private void handleSaving() {

        submitButton.setOnClickListener(v -> {
            v.setEnabled(false);
            if(nameEditText.getText().toString().isEmpty())
            {
                nameEditText.setError("Cannot be Empty");

                v.setEnabled(true);
                return;
            }

            if(watchlistSpinner.getSelectedItem()==null)
            {
                v.setEnabled(true);
                Toast.makeText(getContext(), "select WatchList", Toast.LENGTH_SHORT).show();
                return;
            }

            if(distributionListSpinner.getSelectedItem()==null)
            {
                v.setEnabled(true);
                Toast.makeText(getContext(), "select distribution", Toast.LENGTH_SHORT).show();
                return;
            }

            if(locationSpinner.getSelectedItem()==null)
            {
                v.setEnabled(true);
                Toast.makeText(getContext(), "select Location", Toast.LENGTH_SHORT).show();
                return;
            }

            Alert tosave;
            if(existing==null) {
                tosave = new Alert();
                tosave.setCreated(new Date().getTime());
            }
            else
                tosave=existing;

            tosave.setName(nameEditText.getText().toString());
            tosave.setLocation(locationSpinner.getSelectedItem().toString());
//                tosave.setWatchlist((long) watchlistSpinner.getSelectedItemPosition()+1);
//                tosave.setDistributionList((long) distributionListSpinner.getSelectedItemPosition()+1);

            System.out.println("saved watchlist "+tosave.getWatchlist_id());

            AlertDao alertDao=MyDatabase.getInstance(getContext()).alertDao();



            new Thread(()->{
                tosave.setDistributionList_id(distributionListDao.getByName(distributionListSpinner.getSelectedItem().toString()).getUid());
                tosave.setWatchlist_id(watchlistDao.getByName(watchlistSpinner.getSelectedItem().toString()).getUid());
                if(existing==null)
                {
                    alertDao.insertAlert(tosave);
                }
                else
                {
                    alertDao.updateAlert(tosave);
                }
                new Handler(Looper.getMainLooper()).post(()->{
                    Toast.makeText(getContext(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                    ((MainActivity)getActivity()).removeFragment(AddAlertFragment.class);
                    new AlertView(alertFragment);
                });
            }).start();


        });
    }

    private void setNavigationButtons(View view) {
        View.OnClickListener listener= v -> ((MainActivity)getActivity()).removeFragment(AddAlertFragment.class);

        backButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);

    }

    private void assignAllIds(View view) {
        // ImageView
        backButton = view.findViewById(R.id.backButton);
        cancelButton = view.findViewById(R.id.cancel_button);
        submitButton = view.findViewById(R.id.submitbutton);

        titleTextView = view.findViewById(R.id.title);
        nameEditText = view.findViewById(R.id.name);
        watchlistSpinner = view.findViewById(R.id.watchlist);
        distributionListSpinner = view.findViewById(R.id.distributionlist);
        locationSpinner = view.findViewById(R.id.location);

    }

    private void setSources() {
        List<String> locations = new ArrayList<>();
        locations.add("All");

        for (CameraInfo c : App.getmCameraInfos()) {
            locations.add(c.location);
        }

        locationSpinner.setAdapter(new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, locations));

        new Thread(() -> {
            List<String> watchLists = new ArrayList<>();;
            List<String> distLists = new ArrayList<>();;

//            watchLists.add("All");
//            distLists.add("All");

            watchLists.addAll(watchlistDao.listAll());
            distLists.addAll(distributionListDao.listNames());


            String selectedWatchList="";
            String selectedDistr="";
            if(existing!=null) {
                selectedWatchList = watchlistDao.getByNum(existing.getWatchlist_id()).getName();
                selectedDistr = distributionListDao.getDistributionListById(existing.getDistributionList_id()).getName();
            }

            String finalSelectedWatchList = selectedWatchList;
            String finalSelectedDistr = selectedDistr;
            new Handler(Looper.getMainLooper()).post(() -> {
                watchlistSpinner.setAdapter(new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, watchLists));
                distributionListSpinner.setAdapter(new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, distLists));

                if(existing!=null)
                {
                    titleTextView.setText("Edit Alert");
                    nameEditText.setText(existing.getName());

                    watchlistSpinner.setSelection(watchLists.indexOf(finalSelectedWatchList));
                    distributionListSpinner.setSelection(distLists.indexOf(finalSelectedDistr));

                    locationSpinner.setSelection(locations.indexOf(existing.getLocation()));

                    View deleteIcon=getView().findViewById(R.id.deleteIcon);
                    deleteIcon.setVisibility(View.VISIBLE);

                    final AlertDao alertDao=MyDatabase.getInstance(getContext()).alertDao();

                    deleteIcon.setOnClickListener(v -> {
                        PromptDialog promptDialog=new PromptDialog(getContext());
                        promptDialog.setText("Are you Sure You want to delete this Alert "+" ? ");
                        promptDialog.setOnAcceptListener(v1 -> {
                            System.out.println("deleting");
                            new Thread()
                            {
                                public void run()
                                {
                                    try{
                                        alertDao.delete(existing);

                                        new Handler(Looper.getMainLooper()).post(()->{
                                            Toast.makeText(v1.getContext(), "Deleted SuccessFully", Toast.LENGTH_SHORT).show();
                                            ((MainActivity)getActivity()).removeFragment(AddAlertFragment.class);
                                            new AlertView(alertFragment);
                                        });
                                    }catch (Exception ed)
                                    {
                                        ed.printStackTrace();
                                    }
                                }
                            }.start();
                            promptDialog.dismiss();
                        });
                        promptDialog.show();
                    });

                }
            });
        }).start();
    }



}