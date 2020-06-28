package com.facecoolalert.utils.Alert;

import android.content.Context;

import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.AlertDao;
import com.facecoolalert.database.Repositories.AlertLogDao;
import com.facecoolalert.database.Repositories.DistributionListDao;
import com.facecoolalert.database.Repositories.SubscriberDistributionListDao;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.database.entities.AlertLog;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.database.entities.Subscriber;
import com.facecoolalert.utils.Alert.email.EmailSender;
import com.facecoolalert.utils.GenUtils;
import com.facecoolalert.utils.Alert.sms.SmsSender;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlertUtils {

    public static ExecutorService threadPool= Executors.newCachedThreadPool();
    public static void sendAlerts(String name, Subscriber subscriber, AlertLog alertLog, RecognitionResult currentRecognition, Subject subject, Context context) {
        AlertDao alertDao= MyDatabase.getInstance(context).alertDao();
        AlertLogDao alertLogDao=MyDatabase.getInstance(context).alertLogDao();
        DistributionListDao distributionListDao=MyDatabase.getInstance(context).distributionListDao();
        SubscriberDistributionListDao subscriberDistributionListDao=MyDatabase.getInstance(context).subscriberDistributionListDao();
        WatchlistDao watchlistDao=MyDatabase.getInstance(context).watchlistDao();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d' 'yyyy h:mm:ss a", Locale.US);

        String message=subject.getName()+" (Watchlist : "+subject.getWatchlist()+") was Identified at "+currentRecognition.getLocation()+" at "+dateFormat.format(new Date(currentRecognition.getDate()));

        String messageSubject="Alert "+name+"! "+subject.getFirstName()+" was Identified at "+currentRecognition.getLocation();

//        if(subscriber.getAlertVia().equals("sms")||subscriber.getAlertVia().equals("both"))
//        {
//            String smsMessage="Alert "+name+" ! "+message;
//            SmsSender.sendSms(smsMessage,subscriber.getPhone(),alertLog,alertDao);
//        }


        threadPool.submit(()->{
        if(subscriber.getAlertVia().equals("sms")||subscriber.getAlertVia().equals("both"))
        {
            SmsSender.sendMms(messageSubject,message,subscriber.getPhone(),alertLog,alertDao,currentRecognition.getBmp(),subject,context);
        }


        if(subscriber.getAlertVia().equals("email")||subscriber.getAlertVia().equals("both"))
        {
            EmailSender.sendEmail(messageSubject,message,subscriber.getEmail(),alertLog,alertDao,currentRecognition.getBmp(),subject,context);
        }
        });

    }

    public static void sendAlerts(String name, List<Subscriber> subscribersForList, AlertLog alertLog, RecognitionResult currentRecognition, Subject subject, Context context) {
        AlertDao alertDao= MyDatabase.getInstance(context).alertDao();
        AlertLogDao alertLogDao=MyDatabase.getInstance(context).alertLogDao();
        DistributionListDao distributionListDao=MyDatabase.getInstance(context).distributionListDao();
        SubscriberDistributionListDao subscriberDistributionListDao=MyDatabase.getInstance(context).subscriberDistributionListDao();
        WatchlistDao watchlistDao=MyDatabase.getInstance(context).watchlistDao();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d' 'yyyy h:mm:ss a", Locale.US);

        String message=subject.getName()+" (Watchlist : "+subject.getWatchlist()+") was Identified at "+currentRecognition.getLocation()+" at "+dateFormat.format(new Date(currentRecognition.getDate()))+" Score ("+ GenUtils.roundScore(currentRecognition.getScoreMatch())+ " %) ";

        String messageSubject="Alert "+name+"! "+subject.getFirstName()+" was Identified at "+currentRecognition.getLocation();

//        if(subscriber.getAlertVia().equals("sms")||subscriber.getAlertVia().equals("both"))
//        {
//            String smsMessage="Alert "+name+" ! "+message;
//            SmsSender.sendSms(smsMessage,subscriber.getPhone(),alertLog,alertDao);
//        }

        threadPool.submit(()->{

            List<String> smsNumbers=new ArrayList<>();
            List<String> emails=new ArrayList<>();
            for(Subscriber subscriber:subscribersForList) {
                if (subscriber.getAlertVia().equals("sms") || subscriber.getAlertVia().equals("both"))
                    if(!subscriber.getPhone().isEmpty()&&subscriber.getPhone().length()>5)
                        smsNumbers.add(subscriber.getPhone());

                String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                Pattern pattern = Pattern.compile(emailPattern);
                Matcher matcher = pattern.matcher(subscriber.getEmail());
                if(subscriber.getAlertVia().equals("email")||subscriber.getAlertVia().equals("both"))
                    if(!subscriber.getEmail().isEmpty() && matcher.matches())
                        emails.add(subscriber.getEmail());

            }
            if(smsNumbers.size()>0)
                SmsSender.sendMms(messageSubject,message,smsNumbers,alertLog,alertDao,currentRecognition.getBmp(),context);

            if(emails.size()>0)
                EmailSender.sendEmail(messageSubject,message,emails,alertLog,alertDao,currentRecognition.getBmp(),subject,context);




        });


    }
}
