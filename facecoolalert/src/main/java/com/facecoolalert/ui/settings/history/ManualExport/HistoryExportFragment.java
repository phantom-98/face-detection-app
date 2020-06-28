package com.facecoolalert.ui.settings.history.ManualExport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facecool.cameramanager.picker.PickerFragment;
import com.facecoolalert.App;
import com.facecoolalert.databinding.FragmentHistoryManualExportBinding;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.settings.history.HistoryFragment;
import com.facecoolalert.ui.subject.multiEnrollment.FolderPicker.FolderPickerFragment;
import com.facecoolalert.utils.PrefManager;
import com.facecoolalert.utils.datetimepicker.DatePickerUtils;
import com.facecoolalert.utils.datetimepicker.TimePickerUtils;

import java.util.Date;

public class HistoryExportFragment extends Fragment {

    private DatePickerUtils dateFrom,dateTo;

    private TimePickerUtils timeFrom,timeTo;


    private CheckBox deleteAfterExport;

    private ProgressBar progressBar;

    private String saveFolder;

    private TextView saveFolderTextView;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentHistoryManualExportBinding binding=FragmentHistoryManualExportBinding.inflate(inflater,container,false);

        dateFrom=new DatePickerUtils(binding.fromDate);
        dateTo=new DatePickerUtils(binding.toDate);

        timeFrom=new TimePickerUtils((binding.fromtime));
        timeTo=new TimePickerUtils((binding.toTime));

        dateFrom.setToday();
        dateTo.setToday();

        timeFrom.setMorning();
        timeTo.setEvening();

        binding.backButton.setOnClickListener((v)->{
            ((MainActivity)getActivity()).removeFragment(getClass());
        });

        deleteAfterExport=binding.deleteAfterExport;
        progressBar=binding.horizontalProgressBar;

        binding.exportHistory.setOnClickListener(v -> {
            v.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            setEnableStatus(binding,false);
            performExport(()->{
                setEnableStatus(binding,true);
                v.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
            });

        });

        handleSaveFolder(binding);

        return binding.getRoot();
    }

    private void handleSaveFolder(FragmentHistoryManualExportBinding binding) {
        saveFolder=PrefManager.getHistoryExportFolder(getContext());
        binding.currentFolder.setText(saveFolder);

        binding.resetButton.setOnClickListener((v -> {
            saveFolder=PrefManager.getHistoryExportFolder(getContext());
            binding.currentFolder.setText(saveFolder);
        }));

        binding.selectFolder.setOnClickListener((v -> {
            HistoryFragment.HistoryFolderChooser pickerFragment = new HistoryFragment.HistoryFolderChooser(saveFolder);
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
                        saveFolder=path;
                        binding.currentFolder.setText(path);
                        ((MainActivity)getActivity()).removeFragment(FolderPickerFragment.class);
                    }
                }
            };

//            pickerFragment.loadLists(PrefManager.getHistoryExportFolder(getContext()));

        }));

    }


    private void setEnableStatus(FragmentHistoryManualExportBinding binding, boolean b) {
        binding.fromDate.setEnabled(b);
        binding.fromtime.setEnabled(b);

        binding.toDate.setEnabled(b);
        binding.toTime.setEnabled(b);

        deleteAfterExport.setEnabled(b);
    }

    private void performExport(Runnable onComplete) {
        Date start=dateFrom.getCombinedTime(timeFrom.getCalendar());
        Date end=dateTo.getCombinedTime(timeTo.getCalendar());
        if(end.before(start)) {
            Toast.makeText(getContext(), "End date/time can`t be before start", Toast.LENGTH_SHORT).show();
            onComplete.run();
            return;
        }
        new Thread(()->ManualExportUtils.exportHistory(start,end,getContext(),progressBar,saveFolder,onComplete,deleteAfterExport.isChecked())).start();
    }
}
