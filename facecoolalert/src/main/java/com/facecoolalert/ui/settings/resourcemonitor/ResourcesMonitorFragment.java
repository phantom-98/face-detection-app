package com.facecoolalert.ui.settings.resourcemonitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facecoolalert.R;
import com.facecoolalert.resources.ResourceManager;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.utils.EditTextUtils;
import com.facecoolalert.utils.GenUtils;
import com.facecoolalert.utils.PrefManager;

public class ResourcesMonitorFragment extends Fragment {

    private View backButton;

    private View view;

    private TextView appMemUsage;
    private TextView memAvailable;
    private TextView appCpuUsage;
    private TextView batteryTemp;

    // Input field
    private EditText thermalLimit;

    private RefreshResourceValues refreshResourceValues;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_resource_monitor,container,false);

        backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener((v)->{
            ((MainActivity)getActivity()).removeFragment(ResourcesMonitorFragment.class);
        });

        appMemUsage = view.findViewById(R.id.appMemUsage);
        memAvailable = view.findViewById(R.id.memAvailable);
        appCpuUsage = view.findViewById(R.id.appCpuUsage);
        batteryTemp = view.findViewById(R.id.batteryTemp);

        // Initialize input field
        thermalLimit = view.findViewById(R.id.thermalLimit);

        refreshResourceValues=new RefreshResourceValues();
        refreshResourceValues.start();


        if(ResourceManager.thermalLimit>0)
            thermalLimit.setText(ResourceManager.thermalLimit+"");


        EditTextUtils.addTextChangeListener(thermalLimit,newText -> {
            PrefManager.setThermalLimit(GenUtils.getInt(newText),getContext());
        });

        return view;
    }



    private class RefreshResourceValues extends Thread{

        public void run()
        {
            while (ResourcesMonitorFragment.this.isAdded())
            {
                new Handler(Looper.getMainLooper()).post(()->{


                try{
                    appMemUsage.setText(String.format("%d MB", MainActivity.resourceManager.getUsedMemory()));
                    memAvailable.setText(String.format("%d MB / %d MB", MainActivity.resourceManager.getAvailableMemory(), MainActivity.resourceManager.getTotalMemory()));
                    appCpuUsage.setText(String.format("%.2f %%", MainActivity.resourceManager.getTotalCpuUsage()));
                    batteryTemp.setText(String.format("%s °C", MainActivity.resourceManager.getDeviceTemperature()));

                }catch (Exception es)
                {
                    es.printStackTrace();
                }
                });

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }


}
