package com.facecoolalert.ui.Alerts.Distribution;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.common.PromptDialog;
import com.facecoolalert.common.subscribersListing.CheckableSubscriberListRecycleViewAdapter;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.AlertDao;
import com.facecoolalert.database.Repositories.DistributionListDao;
import com.facecoolalert.database.Repositories.SubscriberDao;
import com.facecoolalert.database.Repositories.SubscriberDistributionListDao;
import com.facecoolalert.database.entities.DistributionList;
import com.facecoolalert.database.entities.Subscriber;
import com.facecoolalert.database.entities.SubscriberDistributionListCrossRef;
import com.facecoolalert.ui.Alerts.AlertFragment;
import com.facecoolalert.ui.MainActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddDistributionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDistributionFragment extends Fragment {


    private static DistributionList existing;
    private AlertFragment alertFragment;
    private View backButton;


    private Button cancelButton;
    private Button submitButton;


    private TextView titleTextView;
    private EditText nameEditText;
    private EditText searchSubscribersEditText;
    private CheckBox assignAllCheckBox;
    private RecyclerView recyclerView;



    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SubscriberDistributionListDao subscriberDistributionListDao;
    private View view;

    public AddDistributionFragment() {
        // Required empty public constructor
    }

    public AddDistributionFragment(AlertFragment alertFragment, DistributionList distributionList) {
        this.alertFragment=alertFragment;
        existing=distributionList;
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
    public static AddDistributionFragment newInstance(String param1, String param2) {
        AddDistributionFragment fragment = new AddDistributionFragment();
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
        View view=inflater.inflate(R.layout.fragment_add_distribution, container, false);


        assignAllIds(view);
        setNavigationButtons(view);
        if(existing !=null)
        {
            titleTextView.setText("Edit Distribution");
            feedExistingData();
        }
        feedSubscribers();
        return view;
    }



    private void feedExistingData() {
        nameEditText.setText(existing.getName());
        View deleteIcon=view.findViewById(R.id.deleteIcon);
        deleteIcon.setVisibility(View.VISIBLE);
        deleteIcon.setOnClickListener(v -> {
            PromptDialog promptDialog=new PromptDialog(getContext());
            promptDialog.setText("Are you Sure You want to delete this Distribution List "+" ? ");
            promptDialog.setOnAcceptListener(v1 -> {
                new Thread()
                {
                    public void run()
                    {
                        try{
                            DistributionListDao distributionListDao=MyDatabase.getInstance(getContext()).distributionListDao();

                            distributionListDao.delete(existing);
                            try {
                                subscriberDistributionListDao.deleteDistributionListS(existing.getUid());
                            }catch (Exception subje)
                            {
                                subje.printStackTrace();
                            }
                            AlertDao alertDao=MyDatabase.getInstance(getContext()).alertDao();
                            alertDao.deleteByDistributionList(existing.getUid());
                            new Handler(Looper.getMainLooper()).post(()->{
                                Toast.makeText(v1.getContext(), "Deleted SuccessFully", Toast.LENGTH_SHORT).show();
                                ((MainActivity)getActivity()).removeFragment(AddDistributionFragment.class);
                                new DistributionView(alertFragment);
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


    private void setNavigationButtons(View view) {
        View.OnClickListener listener= v -> ((MainActivity)getActivity()).removeFragment(AddDistributionFragment.class);

        backButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);

        handleSaving();

        this.subscriberDistributionListDao= MyDatabase.getInstance(getContext()).subscriberDistributionListDao();
    }




    private void assignAllIds(View view) {
        // ImageView
        backButton = view.findViewById(R.id.backButton);

        this.view=view;


        cancelButton = view.findViewById(R.id.cancel_button);
        submitButton = view.findViewById(R.id.submitbutton);


        backButton = view.findViewById(R.id.backButton);

        titleTextView = view.findViewById(R.id.title);
        nameEditText = view.findViewById(R.id.name);
        searchSubscribersEditText = view.findViewById(R.id.search_subscribers);
        assignAllCheckBox = view.findViewById(R.id.assignall);
        recyclerView = view.findViewById(R.id.recycleView);
        cancelButton = view.findViewById(R.id.cancel_button);
    }


    private CheckableSubscriberListRecycleViewAdapter subscribersAdapter;

    private void feedSubscribers() {

        RecyclerView listView=recyclerView;

        SubscriberDao subscriberDao = MyDatabase.getInstance(getContext()).subscriberDao();


        CompletableFuture.supplyAsync(()->{
            try{
                return subscriberDao.listNames();
            }catch (Exception es){
                es.printStackTrace();
                return Collections.emptyList();
            }
        }).thenAccept(subscribers->{
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            subscribersAdapter = new CheckableSubscriberListRecycleViewAdapter(getContext(), new ArrayList<String>((Collection<? extends String>) subscribers), existing);
            listView.setAdapter(subscribersAdapter);
        });


        searchSubscribersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (subscribersAdapter != null) {
                    // Use runOnUiThread to update the UI
                    subscribersAdapter.filter(s.toString());
                }
                ((ScrollView)  view.findViewById(R.id.scroller)).smoothScrollTo(0, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        assignAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (subscribersAdapter != null) {
                    // Use runOnUiThread to update the UI
                    subscribersAdapter.checkAll();
                }
            } else {
                if (subscribersAdapter != null) {
                    // Use runOnUiThread to update the UI
                    subscribersAdapter.uncheckAll();
                }
            }
        });
        
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

            DistributionList tosave;
            if(existing !=null)
                tosave= existing;
            else
                tosave=new DistributionList();

            tosave.setName(nameEditText.getText().toString());

            if(tosave.getCreated()==null)
                tosave.setCreated(new Date().getTime());

            SubscriberDao subscriberDao=MyDatabase.getInstance(getContext()).subscriberDao();
            DistributionListDao distributionListDao=MyDatabase.getInstance(getContext()).distributionListDao();
            new Thread(()->{

                String distId;
                if(existing ==null) {
                    distributionListDao.insertDistributionList(tosave);
                }else {
                    distributionListDao.updateDistributionList(tosave);
                }
                distId=tosave.getUid();

                subscriberDistributionListDao.deleteDistributionListS(distId);
                String subs="'"+String.join("','", subscribersAdapter.getCheckedItems())+"'";
                System.out.println("subs is "+subs);
                List<Subscriber> subscribers=subscriberDao.getSubscribers(subscribersAdapter.getCheckedItems());

                System.out.println(subscribers.size()+" subscribers "+subscribers);
                for(Subscriber s:subscribers)
                {
                    SubscriberDistributionListCrossRef subscriberDistributionListCrossRef=new SubscriberDistributionListCrossRef();
                    subscriberDistributionListCrossRef.setDistributionList_id(distId);
                    subscriberDistributionListCrossRef.setSubscriber_id(s.getUid());
                    subscriberDistributionListDao.insert(subscriberDistributionListCrossRef);
                }

                new Handler(Looper.getMainLooper()).post(()->{
                    Toast.makeText(getContext(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                    ((MainActivity)getActivity()).removeFragment(AddDistributionFragment.class);
                    new DistributionView(alertFragment);
                });

            }).start();

        });

    }


}