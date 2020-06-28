package com.facecoolalert.ui.settings.history;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facecool.cameramanager.picker.PickerFragment;
import com.facecoolalert.App;
import com.facecoolalert.databinding.FragmentHistoryExportimportBinding;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.base.BaseActivity;
import com.facecoolalert.ui.settings.history.ManualExport.HistoryExportFragment;
import com.facecoolalert.ui.subject.multiEnrollment.FolderPicker.FolderPickerFragment;
import com.facecoolalert.utils.AutoExportRecognitionHistory.AutoExportThread;
import com.facecoolalert.utils.AutoExportRecognitionHistory.AutoExportUtils;
import com.facecoolalert.utils.GenUtils;
import com.facecoolalert.utils.PrefManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HistoryFragment extends Fragment {

    private static final int REQUEST_CODE_PICK_DIRECTORY = 122;
    private static final int REQUEST_CODE_PICK_FILE = 117;
    private FragmentHistoryExportimportBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        binding=FragmentHistoryExportimportBinding.inflate(inflater,container,false);

        binding.backButton.setOnClickListener((v)->{
            ((MainActivity)getActivity()).removeFragment(HistoryFragment.class);
        });

        configureViewItems();

        return binding.getRoot();

    }

    private Thread updatehistorySizeThread;
    private void configureViewItems() {

        binding.currentFolder.setText(PrefManager.getHistoryExportFolder(getContext()));

        binding.historyMaxSize.setText(String.format("%s", PrefManager.getMaxHistorySize(getContext())));
        binding.historyDeleteSize.setText(String.format("%s", PrefManager.getHistoryDeleteSize(getContext())));

        configureListeners();

        updatehistorySizeThread=new Thread(()->{
            while(isAdded())
            {
                try {
                    Double size=BaseActivity.resourceManager.getHistorySize();

                    new Handler(Looper.getMainLooper()).post(()->{
                        binding.historySizeValue.setText(GenUtils.beautifulStorage(size));
                    });

                    Thread.sleep(1500);
                }catch (Exception es)
                {
                    es.printStackTrace();
                }
            }
        });

        updatehistorySizeThread.start();


    }

    private void configureListeners() {

        binding.selectFolder.setOnClickListener((v -> {
            HistoryFolderChooser pickerFragment = new HistoryFolderChooser(PrefManager.getHistoryExportFolder(getContext()));
//                    new FolderPickerFragment(){
//                @Override
//                public void cancel(View v) {
////                    super.cancel(v);
//                    ((MainActivity)getActivity()).removeFragment(FolderPickerFragment.class);
//                }
//            };

            ((MainActivity)getActivity()).addFragment(pickerFragment);//new HistoryFolderChooser());
            pickerFragment.mPickListener= new PickerFragment.OnPickListener() {
                @Override
                public void onPick(String path, boolean isDir) {
                    System.out.println("picked folder "+path+" "+isDir);
                    if(isDir)
                    {
                        PrefManager.setHistoryExportFolder(path,getContext());
                        binding.currentFolder.setText(path);
                        ((MainActivity)getActivity()).removeFragment(FolderPickerFragment.class);
                    }
                }
            };

//            pickerFragment.loadLists(PrefManager.getHistoryExportFolder(getContext()));

        }));

        binding.resetButton.setOnClickListener((v -> {
            PrefManager.setHistoryExportFolder(App.getDefaultHistoryFolder(),getContext());
            binding.currentFolder.setText(PrefManager.getHistoryExportFolder(getContext()));
        }));


//        EditTextUtils.addTextChangeListener(binding.historyMaxSize,newText -> {
//            PrefManager.setMaxHistorySize(MyUtils.getFloat(newText),getContext());
//        });
//
//        EditTextUtils.addTextChangeListener(binding.historyDeleteSize,newText -> {
//            PrefManager.setHistoryDeleteSize(MyUtils.getFloat(newText),getContext());
//        });

        binding.submitSize.setOnClickListener((v -> {
            PrefManager.setMaxHistorySize(GenUtils.getFloat(binding.historyMaxSize.getText().toString().trim()),getContext());
            PrefManager.setHistoryDeleteSize(GenUtils.getFloat(binding.historyDeleteSize.getText().toString().trim()),getContext());
            Toast.makeText(getActivity(),"Save Success ...",Toast.LENGTH_SHORT).show();
        }));

        binding.importExportedHistory.setOnClickListener((v -> {
            openFilePicker();
        }));

        binding.exportHistory.setOnClickListener((v -> {
            ((MainActivity)getActivity()).addFragment(new HistoryExportFragment() );
        }));

    }


    public static class HistoryFolderChooser extends FolderPickerFragment{

        public HistoryFolderChooser(String defaultLocation)
        {
            super();
            location=defaultLocation;
//            loadLists(defaultLocation);
//            showList();
        }

        public HistoryFolderChooser() {
        }

        @Override
        public void cancel(View v) {
//            super.cancel(v);
            ((MainActivity)getActivity()).removeFragment(FolderPickerFragment.class);
        }
    }



    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        Uri uri = Uri.fromFile(new File(AutoExportThread.exportLocation));
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_DIRECTORY && resultCode == RESULT_OK && data != null) {
            // Get the URI of the selected file
            Uri uri = data.getData();
//            saveDataToFile(uri);
        } else if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            // Get the URI of the selected JSON file
            Uri uri = data.getData();

//            importSharedPreferencesFromJson(uri);
            startImporting(uri);

        }
    }

    private void startImporting(Uri uri) {
        binding.importExportedHistory.setEnabled(false);
        new Thread(()->{
            AutoExportUtils.importHistory(uri,getContext(),new ImportProgresChangeRunnable(){
                @Override
                public void run() {
                    binding.horizontalProgressBar.setProgress(getProgres());
                    if(getProgres()>=99) {
                        binding.horizontalProgressBar.setVisibility(View.INVISIBLE);
                        binding.importExportedHistory.setEnabled(true);
                    }
                    else if(getProgres()>0) {
                        binding.horizontalProgressBar.setVisibility(View.VISIBLE);
                        binding.importExportedHistory.setEnabled(false);
                    }
                }
            });
        }).start();
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


    public static interface ImportProgresChangeRunnable extends Runnable{
        class ProgressHolder {
            private static int progress = 0;

            public static int getProgress() {
                return progress;
            }

            public static void setProgress(int value) {
                progress = value;
            }
        }

        public default void setProgres(int progres)
        {
            ProgressHolder.setProgress(progres);
        }

        public default int getProgres()
        {
            return ProgressHolder.getProgress();
        }

    }
}
