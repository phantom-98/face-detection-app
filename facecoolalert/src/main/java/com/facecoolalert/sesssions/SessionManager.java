package com.facecoolalert.sesssions;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SessionManager {

    private static Long SESSION_VALIDITY_TIME = TimeUnit.DAYS.toMillis(7);


    private static String SESSION_STORE_LOCATION="Active_User";


    public static void saveToken(String token, Context context) throws IOException
    {
        File saveLocation=new File(context.getFilesDir(),SESSION_STORE_LOCATION);
        FileOutputStream fileOutputStream=new FileOutputStream(saveLocation);
        fileOutputStream.write(token.getBytes());
    }

    public static Boolean sessionExists(Context context)
    {
        File saveLocation=new File(context.getFilesDir(),SESSION_STORE_LOCATION);
        return saveLocation.exists();
    }

    public static Boolean sessionValid(Context context)
    {
        File saveLocation=new File(context.getFilesDir(),SESSION_STORE_LOCATION);
        if(!saveLocation.exists()||saveLocation.length()==0)
            return false;
        Date validBefore=new Date(saveLocation.lastModified()+SESSION_VALIDITY_TIME);
        return new Date().before(validBefore);
    }

    public static String readToken(Context context) throws IOException
    {
        File saveLocation=new File(context.getFilesDir(),SESSION_STORE_LOCATION);
        FileInputStream fileInputStream=new FileInputStream(saveLocation);
//        return new String();
        Scanner scanner=new Scanner(fileInputStream);
        if(scanner.hasNextLine())
            return scanner.nextLine();
        else
            return "";
    }

    /**
     * Returns the unique identifier for the device
     *
     * @return unique identifier for the device
     */
    public static String getDeviceIMEI(Context context) {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            try {
                deviceUniqueIdentifier = tm.getDeviceId();
            }catch (Exception es)
            {
                es.printStackTrace();
            }
        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
            deviceUniqueIdentifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceUniqueIdentifier;
    }



}
