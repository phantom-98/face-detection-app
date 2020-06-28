package com.facecool.cameramanager.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facecool.cameramanager.R;
import com.facecool.cameramanager.camera.CameraViewFragment;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecool.cameramanager.picker.PickerFragment;
import com.facecool.cameramanager.utils.EditTextUtils;

public class CameraSettingsDialog extends Dialog {


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

    private Button cancelButton;
    private Button submitButton;

    private Boolean haschanged=false;

    private CameraInfo cameraInfo;

    private Activity myActivity;

    private TextView title;
    private TextView resolutionPickLabel;
    private LinearLayout resolutionPick;
    private Spinner sourceResolutionSpinner;



    private CameraViewFragment cameraViewFragment;


    public CameraSettingsDialog(@NonNull Context context) {
        super(context);
        myActivity= (Activity) context;
        init();
    }

    public CameraSettingsDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected CameraSettingsDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    public CameraSettingsDialog(CameraViewFragment cameraViewFragment) {
        super(cameraViewFragment.getContext());
        this.cameraViewFragment=cameraViewFragment;
        init();
    }

    private void init()
    {
        setContentView(R.layout.layout_camera_setting);
        setCancelable(true);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        haschanged = false;

        //initialize variables
        assignAllIds();
        setChangeListeners();
        configureViews();
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

    public void setInitialCamerasValues(CameraInfo cameraInfo) {
                this.cameraInfo=cameraInfo;
        
                locationEditText.setText(cameraInfo.location);
                descriptionEditText.setText(cameraInfo.description);
                sourceTypeSpinner.setSelection(cameraInfo.type + 1, true);
                sourceResolutionSpinner.setSelection(cameraInfo.resolution);

                if (cameraInfo.isFront)
                    frontCameraRadioButton.setChecked(true);
                else
                    backCameraRadioButton.setChecked(true);


                urlEditText.setText(cameraInfo.url);
                usernameEditText.setText(cameraInfo.username);
                passwordEditText.setText(cameraInfo.password);
                fileSelectedText.setText("File : " + cameraInfo.videoPath);


                title.setText("Camera "+(cameraInfo.index+1));


    }


    private void assignAllIds() {


        // Spinners

        sourceTypeSpinner = findViewById(R.id.source_type);
        sourceResolutionSpinner = findViewById(R.id.source_resolution);

// EditTexts
        locationEditText = findViewById(R.id.location);
        descriptionEditText = findViewById(R.id.description);
        urlEditText = findViewById(R.id.url);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

// LinearLayouts
        fileChooseLayout = findViewById(R.id.file_choose);
        selectAndroidCameraLayout = findViewById(R.id.select_android_camera);
        usernameAndPasswordLayout = findViewById(R.id.usernameandpassword);
        resolutionPick = findViewById(R.id.dialog_resolution_layout);

// RadioButtons
        backCameraRadioButton = findViewById(R.id.backCamera);
        frontCameraRadioButton = findViewById(R.id.frontCamera);

// RadioGroup
        selectedAndroidCameraRadioGroup = findViewById(R.id.selected_android_camera);

// TextView
        fileSelectedText = findViewById(R.id.file_selected);
        resolutionPickLabel = findViewById(R.id.dialog_resolition_label);

// Buttons
        selectVideoButton = findViewById(R.id.selectVideo);
        cancelButton = findViewById(R.id.cancel_button);
        submitButton = findViewById(R.id.submitbutton);


        cancelButton.setOnClickListener((e)->{dismiss();});


        title=findViewById(R.id.cam_title);

        submitButton.setOnClickListener((e)->{dismiss();cameraViewFragment.saveSettings();});
    }



    private void pickVideo() {
        PickerFragment fragment = new PickerFragment2(this);


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
            show();
            if (cameraInfo!=null) {
                cameraInfo.videoPath = path;

            }
            fileSelectedText.setText("File : " + path);
        };

        dismiss();
        cameraViewFragment.getActivity().getSupportFragmentManager().beginTransaction()
                .add(cameraViewFragment.containerId, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setChangeListeners() {


        sourceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (cameraInfo!=null) {
                    cameraInfo.type = position - 1;
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
                if (cameraInfo != null) {
                    cameraInfo.resolution = position;
                    haschanged = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Not possible to not select one
            }
        });

        EditTextUtils.addTextChangeListener(locationEditText, newText -> {
            if (cameraInfo!=null) {
                cameraInfo.location = newText;
                haschanged = true;
            }
        });

        EditTextUtils.addTextChangeListener(descriptionEditText, newText -> {
            if (cameraInfo!=null) {
                cameraInfo.description = newText;
                haschanged = true;
            }
        });

        EditTextUtils.addTextChangeListener(urlEditText, newText -> {
            if (cameraInfo!=null) {
                cameraInfo.url = newText;
                haschanged = true;
            }
        });

        EditTextUtils.addTextChangeListener(usernameEditText, newText -> {
            if (cameraInfo!=null) {
                cameraInfo.username = newText;
                haschanged = true;
            }
        });

        EditTextUtils.addTextChangeListener(passwordEditText, newText -> {
            if (cameraInfo!=null) {
                cameraInfo.password = newText;
                haschanged = true;
            }
        });

        selectedAndroidCameraRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //System.out.println("checkid was "+checkedId);

                if (cameraInfo!=null) {
                    cameraInfo.isFront = (checkedId == frontCameraRadioButton.getId());
                    haschanged = true;
                }
            }
        });

        selectVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickVideo();
            }
        });


    }

    public static class PickerFragment2 extends PickerFragment{

        public CameraSettingsDialog dialog;
        PickerFragment2(CameraSettingsDialog dialog)
        {
            this.dialog=dialog;
        }
        @Override
        public void cancel(View v) {
            super.cancel(v);
            dialog.show();
        }
    }


}
