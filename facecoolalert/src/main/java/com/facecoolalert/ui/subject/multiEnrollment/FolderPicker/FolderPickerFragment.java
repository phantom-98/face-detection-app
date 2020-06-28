package com.facecoolalert.ui.subject.multiEnrollment.FolderPicker;

import android.view.View;

import com.facecool.cameramanager.picker.FilePojo;
import com.facecool.cameramanager.picker.PickerFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FolderPickerFragment extends PickerFragment {

    @Override
    public void cancel(View v) {
//        System.out.println("cancelled");
        getActivity().finish();
    }

    @Override
    public void loadLists(String location) {
        try {

            File folder = new File(location);

            if (!folder.isDirectory())
                exit();

            tv_location.setText("Location : " + folder.getAbsolutePath());
            File[] files = folder.listFiles(File::isDirectory);

            foldersList = new ArrayList<>();
            filesList = new ArrayList<>();

            for (File currentFile : files) {
                if (currentFile.isDirectory()) {
                    FilePojo filePojo = new FilePojo(currentFile.getName(), true);
                    foldersList.add(filePojo);
                }
            }

            // sort & add to final List - as we show folders first add folders first to the final list
            Collections.sort(foldersList, comparatorAscending);
            folderAndFileList = new ArrayList<>();
            folderAndFileList.addAll(foldersList);

            //if we have to show files, then add files also to the final list
            if (pickFiles) {
                Collections.sort(filesList, comparatorAscending);
                folderAndFileList.addAll(filesList);
            }

            showList();

        } catch (Exception e) {
            e.printStackTrace();
        }

    } // load List


    @Override
    public void select(View v) {

        {
            if ( mPickListener != null ) {
                mPickListener.onPick(location, true);
                //goBack(null);
            }
        }
    }

}
