package com.facecoolalert.ui.Alerts.Subscribers;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facecoolalert.R;
import com.facecoolalert.common.PromptDialog;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubscriberDao;
import com.facecoolalert.database.entities.Subscriber;
import com.facecoolalert.ui.Alerts.AlertFragment;
import com.facecoolalert.ui.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddSubscriberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddSubscriberFragment extends Fragment {


    private static Subscriber existing;
    private AlertFragment alertFragment;
    private View backButton;


    private Button cancelButton;
    private Button submitButton;


    private EditText nameEditText;
    private RadioGroup alertTypeRadioGroup;
    private RadioButton emailRadioButton;
    private RadioButton smsRadioButton;
    private RadioButton bothRadioButton;
    private EditText emailEditText;
    private EditText smsNumberEditText;





    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView titleTextView;

    public AddSubscriberFragment() {
        // Required empty public constructor
    }

    public AddSubscriberFragment(AlertFragment alertFragment, Subscriber subscriber) {
        this.alertFragment =alertFragment;
        existing=subscriber;

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
    public static AddSubscriberFragment newInstance(String param1, String param2) {
        AddSubscriberFragment fragment = new AddSubscriberFragment();
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
        View view=inflater.inflate(R.layout.fragment_add_subscriber, container, false);


        assignAllIds(view);
        setNavigationButtons(view);
        if(existing!=null) {
            titleTextView.setText("Edit Subscriber");
            feedExistingData(view);
        }

        handleSaving();


        return view;
    }



    private void feedExistingData(View view) {
        nameEditText.setText(existing.getName());
        emailEditText.setText(existing.getEmail());
        smsNumberEditText.setText(existing.getPhone());

        String alertVia= existing.getAlertVia();
        System.out.println("Alert was "+alertVia);
        if(alertVia.equals("email"))
            emailRadioButton.setChecked(true);
        else if (alertVia.equals("sms"))
            smsRadioButton.setChecked(true);
        else
            bothRadioButton.setChecked(true);


        View deleteIcon=view.findViewById(R.id.deleteIcon);
        deleteIcon.setVisibility(View.VISIBLE);
        deleteIcon.setOnClickListener(v -> {
            PromptDialog promptDialog=new PromptDialog(getContext());
            promptDialog.setText("Are you Sure You want to delete this Subscriber "+" ? ");
            promptDialog.setOnAcceptListener(v1 -> {
                System.out.println("deleting");
                new Thread()
                {
                    public void run()
                    {
                        try{
                            SubscriberDao subscriberDao= MyDatabase.getInstance(getContext()).subscriberDao();
                            subscriberDao.delete(existing);
                            try{
                                subscriberDao.deletefromDists(existing.getUid());

                            }catch (Exception es)
                            {
                                es.printStackTrace();
                            }


                            new Handler(Looper.getMainLooper()).post(()->{
                                Toast.makeText(v1.getContext(), "Deleted SuccessFully", Toast.LENGTH_SHORT).show();
                                ((MainActivity)getActivity()).removeFragment(AddSubscriberFragment.class);
                                new SubscriberView(alertFragment);
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
        View.OnClickListener listener= v -> ((MainActivity)getActivity()).removeFragment(AddSubscriberFragment.class);

        backButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);

    }

    private void assignAllIds(View view) {
        // ImageView
        backButton = view.findViewById(R.id.backButton);
        cancelButton = view.findViewById(R.id.cancel_button);
        submitButton = view.findViewById(R.id.submitbutton);

        nameEditText = view.findViewById(R.id.name);
        alertTypeRadioGroup = view.findViewById(R.id.alertType);
        emailRadioButton = view.findViewById(R.id.emailRadio);
        smsRadioButton = view.findViewById(R.id.smsRadio);
        bothRadioButton = view.findViewById(R.id.bothRadio);
        emailEditText = view.findViewById(R.id.email);
        smsNumberEditText = view.findViewById(R.id.smsnumber);

        this.titleTextView = view.findViewById(R.id.title);


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

            if(alertTypeRadioGroup.getCheckedRadioButtonId()==View.NO_ID)
            {
                Toast.makeText(getContext(), "Select Alert Type", Toast.LENGTH_SHORT).show();
                v.setEnabled(true);
                return;
            }

            Subscriber tosave;
            if(existing!=null)
                tosave=existing;
            else
                tosave=new Subscriber();

            tosave.setName(nameEditText.getText().toString());

            if(alertTypeRadioGroup.getCheckedRadioButtonId()==bothRadioButton.getId())
                tosave.setAlertVia("both");
            else if(alertTypeRadioGroup.getCheckedRadioButtonId()==emailRadioButton.getId())
                tosave.setAlertVia("email");
            else if(alertTypeRadioGroup.getCheckedRadioButtonId()==smsRadioButton.getId())
                tosave.setAlertVia("sms");

            if(tosave.getAlertVia()==null)
            {
                System.out.println("Alert produced null");
                tosave.setAlertVia("both");
            }


            tosave.setEmail(emailEditText.getText().toString());
            tosave.setPhone(smsNumberEditText.getText().toString());
            SubscriberDao subscriberDao= MyDatabase.getInstance(getContext()).subscriberDao();
            new Thread(()->{
                if(existing==null)
                {
                    subscriberDao.insertSubscriber(tosave);
                }
                else
                {
                    subscriberDao.updateSubscriber(tosave);
                }
                new Handler(Looper.getMainLooper()).post(()->{
                    Toast.makeText(getContext(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                    ((MainActivity)getActivity()).removeFragment(AddSubscriberFragment.class);
                    new SubscriberView(alertFragment);
                });
            }).start();


        });

    }


}