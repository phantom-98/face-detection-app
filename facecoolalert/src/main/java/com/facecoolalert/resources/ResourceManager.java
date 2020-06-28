package com.facecoolalert.resources;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.resources.cpu.CPUUsage;
import com.facecoolalert.resources.thermal.DeviceTempUtils;

public class ResourceManager {

    private ActivityManager activityManager;
    private Activity activity;

    private Long totalMemory;

    private Long usedMemory;

    private Long availableMemory;
    private long dalvikHeapSize;
    private long nativeHeapSize;


    private Double totalCpuUsage;

    private Double totalAppUsage;

    private Double deviceTemperature;


    private MonitoringThread monitoringThread;

    private Boolean lowMemory;

    public static int thermalLimit=0;

    private RecognitionResultDao recognitionResultDao;

    private Boolean killed=false;


    public ResourceManager(Activity activity)
    {
        this.activity=activity;
        activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        startMonitoring();
        recognitionResultDao= MyDatabase.getInstance(activity.getApplicationContext()).recognitionResultDao();
    }

    public Double getHistorySize()
    {
        Double size=0.0;
        try {
            size = recognitionResultDao.totalBitmapsLength();
            if(size==null) {
                size = 0.0;
                recognitionResultDao= MyDatabase.getInstance(activity.getApplicationContext()).recognitionResultDao();
            }
        }catch (Exception es)
        {
            recognitionResultDao= MyDatabase.getInstance(activity.getApplicationContext()).recognitionResultDao();
//            es.printStackTrace();
        }

        return size;    
    }



    private void startMonitoring() {
        monitoringThread=new MonitoringThread();
        monitoringThread.start();
    }


    public void calculateMemory()
    {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);
        availableMemory = mi.availMem / (1024L*1024L);
        totalMemory=mi.totalMem / (1024L*1024L);

        Debug.MemoryInfo appMemoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(appMemoryInfo);

        Long bts2Mbs=1024L;

       dalvikHeapSize = appMemoryInfo.dalvikPss/bts2Mbs;
       nativeHeapSize = appMemoryInfo.nativePss/bts2Mbs;

       usedMemory= Long.valueOf(appMemoryInfo.getTotalPss())/bts2Mbs;

//       System.out.println("Low Memory : "+mi.lowMemory);
        lowMemory=mi.lowMemory;

    }

    public void calculateCPU()
    {
        Double[] usage=CPUUsage.readUsage();
        totalAppUsage=usage[0];
        totalCpuUsage=usage[1];
    }

    public void calculateTemperature()
    {
        deviceTemperature= DeviceTempUtils.getDeviceTemperature(activity);
    }

    public void killAll() {
        killed=true;
    }

    private class MonitoringThread extends Thread
    {
        public void run()
        {
            while(!activity.isDestroyed()&&!killed)
            {
                try{

                    calculateMemory();
                }catch (Exception es)
                {
                    es.printStackTrace();
                }

                try{
                    calculateCPU();
                }catch (Exception es)
                {
                    es.printStackTrace();
                }
//
                try{
                    calculateTemperature();
                }catch (Exception es)
                {
                    es.printStackTrace();
                }

                try{
                    Thread.sleep(2000);

                }catch (Exception es)
                {

                }
            }

        }
    }


    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public Long getTotalMemory() {
        return totalMemory;
    }

    public Long getUsedMemory() {
        return usedMemory;
    }

    public Long getAvailableMemory() {
        return availableMemory;
    }

    public long getDalvikHeapSize() {
        return dalvikHeapSize;
    }

    public long getNativeHeapSize() {
        return nativeHeapSize;
    }

    public Double getTotalCpuUsage() {
        return totalCpuUsage;
    }

    public Double getTotalAppUsage() {
        return totalAppUsage;
    }

    public Double getDeviceTemperature() {
        return deviceTemperature;
    }

    public Boolean getLowMemory() {
        return lowMemory;
    }


    public Boolean isPastThermalLimit()
    {
        if(thermalLimit>0&&getDeviceTemperature()!=null&&!getDeviceTemperature().isNaN())
        {
            if(getDeviceTemperature()>thermalLimit)
                return true;

        }


        return false;
    }



    @SuppressLint("DefaultLocale")
    public String getCurrentState()
    {
        StringBuilder sb=new StringBuilder();

        sb.append("App Memory Usage: ").append(getUsedMemory()).append(" MB\n");
        sb.append(String.format("Available Memory: %d MB / %d MB\n", getAvailableMemory(), getTotalMemory()));
        sb.append(String.format("App CPU Usage: %f%%\n", getTotalAppUsage()));
        sb.append(String.format("Battery Temperature: %f °C\n", getDeviceTemperature()));

        return sb.toString();
    };
}
