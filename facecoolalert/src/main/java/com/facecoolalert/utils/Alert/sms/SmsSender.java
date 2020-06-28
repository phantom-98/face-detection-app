package com.facecoolalert.utils.Alert.sms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.facecoolalert.database.Repositories.AlertDao;
import com.facecoolalert.database.entities.AlertLog;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.utils.PrefManager;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class SmsSender {


    public static void sendSms(String smsMessage, String phone, AlertLog alertLog, AlertDao alertDao) {
    }

    public static void sendMms(String subject, String message, String phone, AlertLog alertLog, AlertDao alertDao, Bitmap bmp, Subject subject1, Context context) {
        Log.e("MMS Sender", "Let me send call sender");
        MmsSender.sendMms(context,phone,subject,message,bmp);
        sendSms("Alert ! "+message,context,phone);
    }
    public static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // You can change the format and quality as needed
        return stream.toByteArray();
    }

    @SuppressLint("MissingPermission")
    public static void sendSms(String message, Context context, String phone) {
        try {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);

            if (subscriptionManager != null) {
                for (SubscriptionInfo info : subscriptionManager.getActiveSubscriptionInfoList()) {
                    if (info.getSimSlotIndex() == PrefManager.getSMSSimcard(context)) {
                        SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(info.getSubscriptionId());
                        if (smsManager != null) {
//                            smsManager.sendMultimediaMessage(context, mmsUri, null, null, null);
                            smsManager.sendTextMessage(phone,null,message,null,null);
                            return;
                        } else {
                            // Handle the case where SmsManager couldn't be obtained
                            Log.e("MMS Sender", "Failed to get SmsManager for subscription ID: " + info.getSubscriptionId());
                        }
                    }
                }
            } else {
                // Handle the case where SubscriptionManager is null
                Log.e("MMS Sender", "SubscriptionManager is null");
            }

            // Handle the case where no active subscription matched
            Log.e("MMS Sender", "No active subscription found for the desired SIM card");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public static void sendMms(String messageSubject, String message, List<String> smsNumbers, AlertLog alertLog, AlertDao alertDao, Bitmap bmp, Context context) {

        Log.e("MMS Sender", "Let me send call sender");
        MmsSender.sendMms(context,smsNumbers,messageSubject,message,bmp);
        sendSms("Alert ! "+message,context,smsNumbers);
    }

    private static void sendSms(String message, Context context, List<String> smsNumbers) {

        try {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);

            if (subscriptionManager != null) {
                for (SubscriptionInfo info : subscriptionManager.getActiveSubscriptionInfoList()) {
                    if (info.getSimSlotIndex() == PrefManager.getSMSSimcard(context)) {
                        SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(info.getSubscriptionId());
                        if (smsManager != null) {
//                            smsManager.sendMultimediaMessage(context, mmsUri, null, null, null);
//                            smsManager.sendTextMessage(phone,null,message,null,null);
                            for(String phone:smsNumbers)
                                smsManager.sendTextMessage(phone,null,message,null,null);

                            return;
                        } else {
                            // Handle the case where SmsManager couldn't be obtained
                            Log.e("MMS Sender", "Failed to get SmsManager for subscription ID: " + info.getSubscriptionId());
                        }
                    }
                }
            } else {
                // Handle the case where SubscriptionManager is null
                Log.e("MMS Sender", "SubscriptionManager is null");
            }

            // Handle the case where no active subscription matched
            Log.e("MMS Sender", "No active subscription found for the desired SIM card");
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
