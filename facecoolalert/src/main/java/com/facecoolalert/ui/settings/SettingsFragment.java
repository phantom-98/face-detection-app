package com.facecoolalert.ui.settings;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facecool.cameramanager.entity.CameraInfo;
import com.facecool.cameramanager.picker.PickerFragment;
import com.facecoolalert.BuildConfig;
import com.facecoolalert.R;
import com.facecoolalert.common.seekBars.LabeledSeekBar;
import com.facecoolalert.databinding.FragmentSettingsBinding;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.camera.CameraFragment;
import com.facecoolalert.ui.detect.FaceDetectorProcessor;
import com.facecoolalert.utils.EditTextUtils;
import com.facecoolalert.utils.GenUtils;
import com.facecoolalert.utils.PrefManager;
import com.facecoolalert.utils.RecognitionUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private static final int READ_PHONE_STATE_PERMISSION_REQUEST_CODE =116 ;
    private static final int REQUEST_CODE_PICK_FILE = 107;
    private static final String DEFAULT_FILE_NAME = "FaceCoolAlertSettings.json";
    private static final int REQUEST_CODE_PICK_DIRECTORY = 106;
    private View backButton;

    private Button generalSettingsButton;
    private Button cameraSettingsButton;

    private Spinner cameraSourceSpinner;
    private EditText locationEditText;
    private EditText descriptionEditText;
    private Spinner sourceTypeSpinner;
    private LinearLayout fileChooseLayout;
    private TextView fileSelectedText;
    private Button selectVideoButton;
    private LinearLayout selectAndroidCameraLayout;
    private RadioGroup selectedAndroidCameraRadioGroup;
    private RadioButton backCameraRadioButton;
    private RadioButton frontCameraRadioButton;
    private LinearLayout usernameAndPasswordLayout;
    private EditText urlEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button exportButton;
    private Button importButton;
    private Button cancelButton;
    private Button submitButton;

    private SwitchMaterial filter_faces_switch;



    // CardViews
    private CardView controlsCardView;
    private View gensettingsform;
    private View camsettingsform;
    private CardView importExportCardView;
    private CardView saveCancelCardView;

    // ScrollView
    private View camsettingsformScrollView;


    private EditText minutesEditText;

    private EditText secondsEditText;


    private Spinner smsSpinner;

    private Spinner recognitionMode;




    private Boolean loadOnCameraSettings=false;


    //the seekbars
    private LabeledSeekBar matchingThreshold;
    private LabeledSeekBar recognitionImageQualityThreshold;
    private LabeledSeekBar enrollmentImageQualityThreshold;

    private MaterialButton additionalSettings;
    private TextView resolutionPickLabel;
    private LinearLayout resolutionPick;
    private Spinner sourceResolutionSpinner;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;




    public SettingsFragment() {
        // Required empty public constructor
        this.haschanged = false;
        this.loadOnCameraSettings=false;
    }

    public SettingsFragment(boolean camerasSettings) {
        this.haschanged = false;
        this.loadOnCameraSettings=camerasSettings;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
//        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        @NonNull FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        cameraInfos = PrefManager.readCameraSettings(getContext());
        haschanged = false;
        assignAllIds(binding);
        setNavigationButtons(view);

        feedGenSettingsInitialData();
        handleValuesSettings();

        camerasFormConfigure();

        binding.appVersion.setText("App-version: "+ BuildConfig.VERSION_NAME+"   "+BuildConfig.BUILD_DATE);
        return view;
    }

    private void feedGenSettingsInitialData() {
//        progressBar.setProgress((int) (PrefManager.getSimilarityThreshold(getContext())), true);
//        threshold.setText("Matching Threshold : " + progressBar.getProgress());
        matchingThreshold.setDefault((int) PrefManager.getSimilarityThreshold(requireContext().getApplicationContext()));
        enrollmentImageQualityThreshold.setDefault((int) PrefManager.getEnrollmentQualityThreshold(requireContext().getApplicationContext()));
        recognitionImageQualityThreshold.setDefault((int) PrefManager.getRecognitionQualityThreshold(requireContext().getApplicationContext()));


        int interval = PrefManager.getAlertInterval(getContext());
        minutesEditText.setText((interval / 60) + "");
        secondsEditText.setText((interval % 60) + "");
    }

    private int selectedCamera = 0;
    private CameraInfo[] cameraInfos;

    private void camerasFormConfigure() {
        cameraSourceSpinner.setSelection(selectedCamera);

        setInitialCamerasValues();
        configureViews();

        setChangeListeners();

        configureImportExports();

        handleSmsField();


        handleRecognitionModes();
    }







    private void setChangeListeners() {
        cameraSourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCamera = position;
                setInitialCamerasValues();
                configureViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCamera = 0;
                setInitialCamerasValues();
                configureViews();
            }
        });

        sourceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedCamera > 0) {
                    cameraInfos[selectedCamera - 1].type = position - 1;
                    haschanged = true;
                }

                configureViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                configureViews();
            }
        });

        sourceResolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedCamera > 0) {
                    cameraInfos[selectedCamera - 1].resolution = position;
                    haschanged = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Not possible to not select one
            }
        });

        EditTextUtils.addTextChangeListener(locationEditText, newText -> {
            if (selectedCamera > 0) {
                cameraInfos[selectedCamera - 1].location = newText;
                haschanged = true;
            }
        });

        EditTextUtils.addTextChangeListener(descriptionEditText, newText -> {
            if (selectedCamera > 0) {
                cameraInfos[selectedCamera - 1].description = newText;
                haschanged = true;
            }
        });

        EditTextUtils.addTextChangeListener(urlEditText, newText -> {
            if (selectedCamera > 0) {
                cameraInfos[selectedCamera - 1].url = newText;
                haschanged = true;
            }
        });

        EditTextUtils.addTextChangeListener(usernameEditText, newText -> {
            if (selectedCamera > 0) {
                cameraInfos[selectedCamera - 1].username = newText;
                haschanged = true;
            }
        });

        EditTextUtils.addTextChangeListener(passwordEditText, newText -> {
            if (selectedCamera > 0) {
                cameraInfos[selectedCamera - 1].password = newText;
                haschanged = true;
            }
        });

        selectedAndroidCameraRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //System.out.println("checkid was "+checkedId);

                if (selectedCamera > 0) {
                    cameraInfos[selectedCamera - 1].isFront = (checkedId == frontCameraRadioButton.getId());
                    haschanged = true;
                }
            }
        });


    }

    private void configureViews() {
        hideAll();
        if(sourceTypeSpinner.getSelectedItem()==null){

        } else if (sourceTypeSpinner.getSelectedItem().equals("IP Camera")||sourceTypeSpinner.getSelectedItem().equals("Wifi Camera")) {
            usernameAndPasswordLayout.setVisibility(View.VISIBLE);
        } else if (sourceTypeSpinner.getSelectedItem().equals("Android")) {
            selectAndroidCameraLayout.setVisibility(View.VISIBLE);
        } else if (sourceTypeSpinner.getSelectedItem().equals("Video file")) {
            fileChooseLayout.setVisibility(View.VISIBLE);
        } else if (sourceTypeSpinner.getSelectedItem().equals("Drone RC")) {
            resolutionPickLabel.setVisibility(View.VISIBLE);
            resolutionPick.setVisibility(View.VISIBLE);
        }

    }

    private void hideAll() {
        usernameAndPasswordLayout.setVisibility(View.GONE);
        selectAndroidCameraLayout.setVisibility(View.GONE);
        //selectAndroidCameraLayout.setVisibility(View.GONE);
        fileChooseLayout.setVisibility(View.GONE);
        resolutionPick.setVisibility(View.GONE);
        resolutionPickLabel.setVisibility(View.GONE);
    }

    private void setInitialCamerasValues() {
//        System.out.println("calling initial values");
        if (cameraInfos == null || cameraInfos.length < 4) {
            cameraInfos = new CameraInfo[4];
            for (int i = 0; i < 4; i++){
                cameraInfos[i] = new CameraInfo();
                cameraInfos[i].index=i;
            }
        }

        if (selectedCamera > 0) {
            try {

                locationEditText.setText(cameraInfos[selectedCamera - 1].location);
                descriptionEditText.setText(cameraInfos[selectedCamera - 1].description);
                sourceTypeSpinner.setSelection(cameraInfos[selectedCamera - 1].type + 1, true);
                sourceResolutionSpinner.setSelection(cameraInfos[selectedCamera - 1].resolution);

                if (cameraInfos[selectedCamera - 1].isFront)
                    frontCameraRadioButton.setChecked(true);
                else
                    backCameraRadioButton.setChecked(true);


                urlEditText.setText(cameraInfos[selectedCamera - 1].url);
                usernameEditText.setText(cameraInfos[selectedCamera - 1].username);
                passwordEditText.setText(cameraInfos[selectedCamera - 1].password);
                fileSelectedText.setText("File : " + cameraInfos[selectedCamera - 1].videoPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleValuesSettings() {

        //seekbars
        matchingThreshold.setLabel(getString(R.string.matching_threshold));
        recognitionImageQualityThreshold.setLabel(getString(R.string.recognition_quality_threshold));
        enrollmentImageQualityThreshold.setLabel(getString(R.string.enrollment_quality_threshold));

        matchingThreshold.setOnChangeAction(()->{
            PrefManager.setSimilarityThreshold(requireContext().getApplicationContext(),matchingThreshold.getValue());
        });
        recognitionImageQualityThreshold.setOnChangeAction(()->{
            PrefManager.setRecognitionQualityThreshold(requireContext().getApplicationContext(),recognitionImageQualityThreshold.getValue());
        });
        enrollmentImageQualityThreshold.setOnChangeAction(()->{
            PrefManager.setEnrollmentQualityThreshold(requireContext().getApplicationContext(),enrollmentImageQualityThreshold.getValue());
        });


        EditTextUtils.addTextChangeListener(minutesEditText, newText -> {
//            minutesEditText.setText();
            int interval = GenUtils.getInt(minutesEditText.getText().toString()) * 60 + GenUtils.getInt(secondsEditText.getText().toString());
            if (interval < 30) {
                minutesEditText.setError("Interval cannot be less than 30 s");
                secondsEditText.setError("Interval cannot be less than 30 s");
            } else {
                PrefManager.setAlertInterval(interval, getContext());
                minutesEditText.setError(null);
                secondsEditText.setError(null);
            }
        });

        EditTextUtils.addTextChangeListener(secondsEditText, newText -> {
//            minutesEditText.setText();
            int interval = GenUtils.getInt(minutesEditText.getText().toString()) * 60 + GenUtils.getInt(secondsEditText.getText().toString());
            if (interval < 30) {
                minutesEditText.setError("Interval cannot be less than 30 s");
                secondsEditText.setError("Interval cannot be less than 30 s");
            } else {
                PrefManager.setAlertInterval(interval, getContext());
                minutesEditText.setError(null);
                secondsEditText.setError(null);
            }
        });


    }

    private Boolean haschanged;

    @SuppressLint("ClickableViewAccessibility")
    private void setNavigationButtons(View view) {
        View.OnClickListener listener = v -> {
            CameraFragment.updateColorAroundScreen();
            if (!haschanged)
                ((MainActivity) view.getContext()).clearExistingFlagments();
            else {


                new AlertDialog.Builder(getActivity()).setMessage("Camera setting Might have been modified.\nWould you like to ignore changes?")
                        .setNegativeButton("No", (dialog, i) -> {

                        })
                        .setPositiveButton("Yes", ((dialogInterface, i) -> {
                            ((MainActivity) view.getContext()).clearExistingFlagments();
                        })).create().show();
            }
        };

        backButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);

        //System.out.println("cam info is "+Arrays.toString(((MainActivity) view.getContext()).getApp().getCameraInfos()));


        generalSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camsettingsform.setVisibility(View.GONE);
                gensettingsform.setVisibility(View.VISIBLE);
                v.setBackgroundColor(Color.parseColor("#132558"));
                cameraSettingsButton.setBackgroundColor(Color.TRANSPARENT);

                generalSettingsButton.setTextColor(Color.WHITE);
                cameraSettingsButton.setTextColor(Color.parseColor("#333333"));

                saveCancelCardView.setVisibility(View.GONE);
                importExportCardView.setVisibility(View.VISIBLE);
            }
        });

        cameraSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gensettingsform.setVisibility(View.GONE);
                camsettingsform.setVisibility(View.VISIBLE);

                v.setBackgroundColor(Color.parseColor("#132558"));
                generalSettingsButton.setBackgroundColor(Color.TRANSPARENT);

                cameraSettingsButton.setTextColor(Color.WHITE);
                generalSettingsButton.setTextColor(Color.parseColor("#333333"));

                importExportCardView.setVisibility(View.GONE);
                saveCancelCardView.setVisibility(View.VISIBLE);
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                CameraFragment.updateColorAroundScreen();
                Toast.makeText(getActivity(), "Saving Info ...", Toast.LENGTH_SHORT).show();
                PrefManager.saveCameraSettings(getContext(), cameraInfos);
                ((MainActivity) view.getContext()).getApp().reloadCameraInfo();
                ((MainActivity) view.getContext()).reloadCameraInfos();
                //((MainActivity)view.getContext()).setCameraFragment();
                ((MainActivity) view.getContext()).handlerActivity.sendMessage(Message.obtain(((MainActivity) view.getContext()).handlerActivity, 2, cameraInfos));

            }
        });

        selectVideoButton.setOnClickListener(v -> pickVideo());

        if(!loadOnCameraSettings)
            generalSettingsButton.performClick();
    }



    private void assignAllIds(FragmentSettingsBinding binding) {
            // ImageView
            backButton = binding.backButton;

            // Buttons
            generalSettingsButton = binding.generalSettings;
            cameraSettingsButton = binding.cameraSettings;


            // Spinners
            cameraSourceSpinner = binding.cameraSource;
            sourceTypeSpinner = binding.sourceType;
            sourceResolutionSpinner = binding.sourceResolution;

            // EditTexts
            locationEditText = binding.location;
            descriptionEditText = binding.description;
            urlEditText = binding.url;
            usernameEditText = binding.username;
            passwordEditText = binding.password;

            // LinearLayouts
            fileChooseLayout = binding.fileChoose;
            selectAndroidCameraLayout = binding.selectAndroidCamera;
            usernameAndPasswordLayout = binding.usernameandpassword;
            resolutionPick = binding.resolutionPick;

            // RadioButtons
            backCameraRadioButton = binding.backCamera;
            frontCameraRadioButton = binding.frontCamera;

            // RadioGroup
            selectedAndroidCameraRadioGroup = binding.selectedAndroidCamera;

            // TextView
            fileSelectedText = binding.fileSelected;
            resolutionPickLabel = binding.resolutionPickLabel;

            // Buttons
            selectVideoButton = binding.selectVideo;
            exportButton = binding.exportButton;
            importButton = binding.importbutton;
            cancelButton = binding.cancelButton;
            submitButton = binding.submitbutton;

            controlsCardView = binding.controls;
            gensettingsform = binding.gensettingsform;
            camsettingsform = binding.camsettingsform;
            importExportCardView = binding.importExport;
            saveCancelCardView = binding.savecancel;

            // ScrollView
            camsettingsformScrollView = binding.camsettingsform;



            minutesEditText = binding.minutes;
            secondsEditText = binding.seconds;

            smsSpinner = binding.smssimcard;
            recognitionMode = binding.recognitionMode;

            //The seekbars
            matchingThreshold=binding.recognitionThreshold;
            recognitionImageQualityThreshold=binding.recognitionImageQualityThreshold;
            enrollmentImageQualityThreshold=binding.enrollmentImageQualityThreshold;


            additionalSettings=binding.additionalSettings;
            additionalSettings.setOnClickListener((v -> {
                ((MainActivity)getActivity()).addFragment(new AdditionalSettingsFragment());
            }));


            filter_faces_switch= binding.filterFaces;
            filter_faces_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    PrefManager.setBooleanFilterFaces(isChecked,getContext());
                    Log.d("Filter Faces",isChecked+"");
                }
            });



            filter_faces_switch.setChecked(FaceDetectorProcessor.filterFaces);

    }



    private void pickVideo() {
        PickerFragment fragment = new PickerFragment();


        fragment.filters = new String[]{
                ".mp4",
                ".avi",
                ".mov",
                ".wmv",
                ".flv",
                ".mkv",
                ".mpg",
                ".3gp",
                ".webm",
                ".m4v",
        };

        fragment.mPickListener = (path, isDir) -> {
            if (selectedCamera > 0) {
                cameraInfos[selectedCamera - 1].videoPath = path;

            }
            fileSelectedText.setText("File : " + path);
        };

        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .commit();
    }


    private void configureImportExports() {
        exportButton.setOnClickListener(v -> fileSavePicker());
        importButton.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }


    private void fileSavePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, DEFAULT_FILE_NAME);
        startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_DIRECTORY && resultCode == RESULT_OK && data != null) {
            // Get the URI of the selected file
            Uri uri = data.getData();
            saveDataToFile(uri);
        } else if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            // Get the URI of the selected JSON file
            Uri uri = data.getData();

            importSharedPreferencesFromJson(uri);
        }
    }

    private String readJsonFromFile(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    private void importSharedPreferencesFromJson(Uri uri) {
        try {


            String jsonData = readJsonFromFile(uri);
            // Parse the JSON data into a JSONObject
            JSONObject sharedPreferencesData = new JSONObject(jsonData);
            if (sharedPreferencesData.get("app") != null && sharedPreferencesData.getString("app").equals("FaceCoolAlert")) {
                importDataIntoSharedPreferences(sharedPreferencesData);
                Toast.makeText(getContext(), "settings imported SuccessFully", Toast.LENGTH_SHORT).show();
                RecognitionUtils.similarityThreshold = PrefManager.getSimilarityThreshold(getContext());
                cameraInfos = PrefManager.readCameraSettings(getContext());
                ((MainActivity) getContext()).getApp().reloadCameraInfo();
                ((MainActivity) getContext()).reloadCameraInfos();
                //((MainActivity)view.getContext()).setCameraFragment();
                ((MainActivity) getContext()).handlerActivity.sendMessage(Message.obtain(((MainActivity) getContext()).handlerActivity, 2, cameraInfos));
                feedGenSettingsInitialData();
                setInitialCamerasValues();

            } else {
                Toast.makeText(getContext(), "settings not for FaceCoolAlert App", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Unable to open settings file", Toast.LENGTH_SHORT).show();
        }
    }


    private void importDataIntoSharedPreferences(JSONObject sharedPreferencesData) throws JSONException {
        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Clear existing SharedPreferences data (optional, depending on your use case)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Iterate through the JSON data and import it into SharedPreferences
        SharedPreferences.Editor dataEditor = sharedPreferences.edit();
        for (Iterator<String> it = sharedPreferencesData.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = sharedPreferencesData.get(key);

            // Determine the data type and import it into SharedPreferences accordingly
            if (value instanceof String) {
                dataEditor.putString(key, (String) value);
            } else if (value instanceof Integer) {
                dataEditor.putInt(key, (Integer) value);
            } else if (value instanceof Boolean) {
                dataEditor.putBoolean(key, (Boolean) value);
            }

            // Add additional data types as needed
        }
        dataEditor.apply();
    }

    private void saveDataToFile(Uri uri) {
        try {
            // Take write permission for the URI
            getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // Open an OutputStream to write data to the selected file
            OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);

            if (outputStream != null) {
                // Write your actual data here
                String dataToWrite = getAppData().toString();
                byte[] dataBytes = dataToWrite.getBytes("UTF-8");
                outputStream.write(dataBytes);

                // Close the OutputStream
                outputStream.close();

                // Show a success message
                Toast.makeText(getContext(), "Exported Successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Handle the case where outputStream is null
                Toast.makeText(getContext(), "Error: OutputStream is null", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error saving data to the file.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject getAppData() {
        JSONObject sharedPreferencesData = new JSONObject();
        try {
            // Retrieve SharedPreferences data
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            // Get all key-value pairs from SharedPreferences and add them to the JSON object
            Map<String, ?> allEntries = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                sharedPreferencesData.put(key, value);
            }
            sharedPreferencesData.put("app", "FaceCoolAlert");
        } catch (JSONException e) {
            e.printStackTrace();

        }
        return sharedPreferencesData;
    }


    private void handleSmsField() {

        populateSimCards();
        smsSpinner.setSelection(PrefManager.getSMSSimcard(getContext()));
        smsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PrefManager.setSmsSimcard(position,getContext());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void populateSimCards() {
        TelephonyManager telephonyManager = getActivity().getSystemService(TelephonyManager.class);
        if (telephonyManager != null) {
            List<String> simCardInfoList = new ArrayList<>();
            SubscriptionManager subscriptionManager = getActivity().getSystemService(SubscriptionManager.class);
            if (subscriptionManager != null) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_PERMISSION_REQUEST_CODE);
                    return;
                }
                List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                if (subscriptionInfoList != null && !subscriptionInfoList.isEmpty()) {
                    for (SubscriptionInfo info : subscriptionInfoList) {
                        String simCardInfo = "SIM " + (info.getSimSlotIndex()+1) + ": " + info.getDisplayName();
                        simCardInfoList.add(simCardInfo);
                    }
                }
            }
            // Create an ArrayAdapter to display SIM card info in the Spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, simCardInfoList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            smsSpinner.setAdapter(adapter);

            smsSpinner.setSelection(PrefManager.getSMSSimcard(getContext()));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_PHONE_STATE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateSimCards();
            }
        }
    }

    private void handleRecognitionModes() {
        recognitionMode.setSelection(PrefManager.getRecognitionMode(getContext()));
        recognitionMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PrefManager.setRecognitionMode(position,getContext());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}