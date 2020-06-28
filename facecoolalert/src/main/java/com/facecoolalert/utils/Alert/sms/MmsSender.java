package com.facecoolalert.utils.Alert.sms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

public class MmsSender {


    public static void sendMms(Context context, String phone, String subject, String messageContent, Bitmap bmp) {
        try {
            Log.e("MMS Sender", "Let me send");
            Settings settings = new Settings();
            settings.setUseSystemSending(true);
//            settings.setProxy("172.22.2.38");
//            settings.setPort("8080");
            Transaction transaction = new Transaction(context, settings);
            Message message = new Message(messageContent, phone);
            message.setImage(bmp);
            transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
            Log.e("MMS Sender", "finished sending");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendMms(Context context, List<String> smsNumbers, String messageSubject, String messageContent, Bitmap bmp) {

        try {
            Log.e("MMS Sender", "Let me send");
            Settings settings = new Settings();
            settings.setUseSystemSending(true);
//            settings.setProxy("172.22.2.38");
//            settings.setPort("8080");
            Transaction transaction = new Transaction(context, settings);
            for(String phone:smsNumbers) {
                try {
                    Message message = new Message(messageContent, phone);
                    message.setImage(bmp);
                    transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
                }catch (Exception esf)
                {
                    esf.printStackTrace();
                }
            }
            Log.e("MMS Sender", "finished sending");
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
