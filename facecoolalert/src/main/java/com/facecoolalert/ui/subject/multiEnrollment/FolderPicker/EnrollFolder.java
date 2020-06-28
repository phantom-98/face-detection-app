package com.facecoolalert.ui.subject.multiEnrollment.FolderPicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.facecool.cameramanager.picker.PickerFragment;
import com.facecoolalert.R;
import com.facecoolalert.ui.subject.multiEnrollment.AddFolderFragment;
import com.facecoolalert.ui.subject.multiEnrollment.FolderPicker.FolderPickerFragment;

public class EnrollFolder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_enroll);

        FrameLayout container = findViewById(R.id.fragment_container);

        if (savedInstanceState == null) {
            // Create a new instance of your fragment
            PickerFragment pickerFragment = new FolderPickerFragment();

            // Start a fragment transaction
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Replace the container with the fragment
            transaction.replace(container.getId(), pickerFragment);

            // Commit the transaction
            transaction.commit();


            pickerFragment.mPickListener=new PickerFragment.OnPickListener() {
                @Override
                public void onPick(String path, boolean isDir) {
                    if(isDir)
                    {
                        System.out.println("Picked Directory : "+path);
                        getSupportFragmentManager().beginTransaction().replace(container.getId(),new AddFolderFragment(path)).commit();

                    }

                }
            };
        }
    }
}