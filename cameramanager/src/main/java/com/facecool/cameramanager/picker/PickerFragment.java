package com.facecool.cameramanager.picker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facecool.cameramanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PickerFragment extends Fragment {

    public interface OnPickListener {
        public void onPick(String path, boolean isDir);
    }


    //Folders and Files have separate lists because we show all folders first then files
    protected ArrayList<FilePojo> folderAndFileList;
    protected ArrayList<FilePojo> foldersList;
    protected ArrayList<FilePojo> filesList;

    public String[] filters;

    public OnPickListener mPickListener;

    TextView tv_title;
    protected TextView tv_location;

    protected String location = Environment.getExternalStorageDirectory().getAbsolutePath();
    public boolean pickFiles = true;
    Intent receivedIntent;

    View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fp_main_layout, null);
        tv_title = (TextView) rootView.findViewById(R.id.fp_tv_title);
        tv_location = (TextView) rootView.findViewById(R.id.fp_tv_location);

        rootView.findViewById(R.id.tv_go_back).setOnClickListener(this::goBack);
        rootView.findViewById(R.id.fp_btn_new).setOnClickListener(this::newFolderDialog);
        rootView.findViewById(R.id.fp_btn_select).setOnClickListener(this::select);
        rootView.findViewById(R.id.tv_cancel).setOnClickListener(this::cancel);
        loadLists(location);
        return rootView;
    }


    /* Checks if external storage is available to at least read */
    boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    protected void loadLists(String location) {
        try {

            File folder = new File(location);

            if (!folder.isDirectory())
                exit();

            tv_location.setText("Location : " + folder.getAbsolutePath());
            File[] files = folder.listFiles();

            foldersList = new ArrayList<>();
            filesList = new ArrayList<>();

            for (File currentFile : files) {
                if (currentFile.isDirectory()) {
                    FilePojo filePojo = new FilePojo(currentFile.getName(), true);
                    foldersList.add(filePojo);
                } else {
                    String filename = currentFile.getName();
                    boolean isFiltered =  true;
                    if ( filters != null && filters.length > 0 ) {
                        isFiltered = false;
                        for ( String ext : filters ) {
                            if ( filename.endsWith(ext)) {
                                isFiltered = true;
                                break;
                            }
                        }
                    }

                    if ( isFiltered ) {
                        FilePojo filePojo = new FilePojo(filename, false);
                        filesList.add(filePojo);
                    }
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


    protected Comparator<FilePojo> comparatorAscending = new Comparator<FilePojo>() {
        @Override
        public int compare(FilePojo f1, FilePojo f2) {
            return f1.getName().compareTo(f2.getName());
        }
    };


    protected void showList() {

        try {
            FolderAdapter FolderAdapter = new FolderAdapter(getActivity(), folderAndFileList);
            ListView listView = (ListView) rootView.findViewById(R.id.fp_listView);
            listView.setAdapter(FolderAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    listClick(position);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    void listClick(int position) {

        if (pickFiles && !folderAndFileList.get(position).isFolder()) {
            String data = location + File.separator + folderAndFileList.get(position).getName();
            if ( mPickListener != null ) {
                mPickListener.onPick(data, false);
                exit();
            }
        } else {
            location = location + File.separator + folderAndFileList.get(position).getName();
            loadLists(location);
        }

    }

    public void goBack(View v) {

        if (location != null && !location.equals("") && !location.equals("/")) {
            int start = location.lastIndexOf('/');
            String newLocation = location.substring(0, start);
            location = newLocation;
            loadLists(location);
        } else {
            exit();
        }

    }

    protected void exit() {
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    void createNewFolder(String filename) {
        try {

            File file = new File(location + File.separator + filename);
            file.mkdirs();
            loadLists(location);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error:" + e.toString(), Toast.LENGTH_LONG)
                    .show();
        }

    }

    public void newFolderDialog(View v) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setTitle("Enter Folder Name");

        final EditText et = new EditText(getActivity());
        dialog.setView(et);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Create",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        createNewFolder(et.getText().toString());
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        dialog.show();

    }


    public void select(View v) {

        if (pickFiles) {
            Toast.makeText(getActivity(), "You have to select a file", Toast.LENGTH_LONG).show();
        } else {
            if ( mPickListener != null ) {
                mPickListener.onPick(location, true);
                goBack(null);
            }
        }
    }


    public void cancel(View v) {
        exit();
    }


} // class
