package com.facecoolalert.ui.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ViewDataBinding;

import com.facecoolalert.App;
import com.facecoolalert.resources.ResourceManager;
import com.facecoolalert.utils.AutoExportRecognitionHistory.AutoExportThread;
import com.facecoolalert.utils.PrefManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 112;
    public static ResourceManager resourceManager;

    public static AutoExportThread autoExportThread;

    public App getApp() {
        return (App) getApplication();
    }
    public abstract ViewDataBinding createBinding();

    public static int prevNavigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewDataBinding binding = createBinding();


        if(resourceManager!=null)
            resourceManager.killAll();
       resourceManager=new ResourceManager(this);
       ResourceManager.thermalLimit= PrefManager.getThermalLimit(this);


       if(autoExportThread!=null)
           autoExportThread.killAll();

       autoExportThread=new AutoExportThread(getApplicationContext());
       autoExportThread.start();
//
//       new Thread(()->{
//           System.out.println("History size : "+resourceManager.getHistorySize());
//       }).start();


       //below prints System resources Information
        final Boolean doPrint=false;
       new Thread(()->{
           while (doPrint)
           {
               {
                   System.out.println("System Information:");
                   System.out.println("Total Memory: " + resourceManager.getTotalMemory() + " MB");
                   System.out.println("Used Memory: " + resourceManager.getUsedMemory() + " MB");
                   System.out.println("Available Memory: " + resourceManager.getAvailableMemory() + " MB");
                   System.out.println("java Heap Size: " + resourceManager.getDalvikHeapSize() + " MB");
                   System.out.println("Native Heap Size: " + resourceManager.getNativeHeapSize() + " MB");
                   System.out.println("Total CPU Usage: " + resourceManager.getTotalCpuUsage() + "%");
                   System.out.println("Total App Usage: " + resourceManager.getTotalAppUsage() + "%");
                   System.out.println("CPU Temperature: " + resourceManager.getDeviceTemperature() + " °C");

               }

               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }
       }).start();
    }









}
