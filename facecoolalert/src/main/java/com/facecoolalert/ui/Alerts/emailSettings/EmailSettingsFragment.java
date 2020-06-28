package com.facecoolalert.ui.Alerts.emailSettings;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.facecoolalert.R;
import com.facecoolalert.utils.Alert.email.EmailSettings;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.utils.PrefManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmailSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmailSettingsFragment extends Fragment {


    private View backButton;


    private Button cancelButton;
    private Button submitButton;


    private Spinner service;
    private EditText smtphost;
    private EditText port;
    private Spinner encryption;
    private EditText email;
    private EditText password;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EmailSettingsFragment() {
        // Required empty public constructor
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
    public static EmailSettingsFragment newInstance(String param1, String param2) {
        EmailSettingsFragment fragment = new EmailSettingsFragment();
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
        View view=inflater.inflate(R.layout.fragment_email_settings, container, false);


        assignAllIds(view);
        setNavigationButtons(view);


        loadExistingData();

        handleSaving();


        return view;
    }

    private EmailSettings emailSettings;
    private void loadExistingData() {
        emailSettings= PrefManager.readEmailSettings(getContext());

        try {
            smtphost.setText(emailSettings.getSmtpHost());
            port.setText(String.valueOf(emailSettings.getPort()));
            service.setSelection(getIndex(service, emailSettings.getService()));
            encryption.setSelection(getIndex(encryption, emailSettings.getEncryption()));
            email.setText(emailSettings.getEmail());
            password.setText(emailSettings.getPassword());
        }catch (Exception ed)
        {
            ed.printStackTrace();
        }


    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0; // Default to the first item if not found
    }


    private void handleSaving() {

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emailSettings.setSmtpHost(smtphost.getText().toString());
                emailSettings.setPort(Integer.parseInt(port.getText().toString()));
                emailSettings.setService(service.getSelectedItem().toString());
                emailSettings.setEncryption(encryption.getSelectedItem().toString());
                emailSettings.setEmail(email.getText().toString());
                emailSettings.setPassword(password.getText().toString());

                PrefManager.saveEmailSettings(getContext(),emailSettings);
                Toast.makeText(getContext(), "Saved Successfully", Toast.LENGTH_SHORT).show();

                {
                    new AlertDialog.Builder(getActivity()).setTitle("Save Success").setMessage("\nWould you like to close Email Settings or continue editing?")
                            .setNegativeButton("Continue Editing", (dialog, i) -> {

                            })
                            .setPositiveButton("Close", ((dialogInterface, i) -> {
                                ((MainActivity)getActivity()).removeFragment(EmailSettingsFragment.class);
                            })).create().show();
                }

            }
        });


    }


    private void setNavigationButtons(View view) {
        View.OnClickListener listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MainActivity)getActivity()).removeFragment(EmailSettingsFragment.class);


            }
        };
        backButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);

            }
        });
    }


    private void assignAllIds(View view) {
        // ImageView
        backButton = view.findViewById(R.id.backButton);



        cancelButton = view.findViewById(R.id.cancel_button);
        submitButton = view.findViewById(R.id.submitbutton);


        service = view.findViewById(R.id.service);
        smtphost = view.findViewById(R.id.smtphost);
        port = view.findViewById(R.id.port);
        encryption = view.findViewById(R.id.encryption);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
    }
}