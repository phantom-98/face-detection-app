package com.facecoolalert.utils.Alert.email;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.facecoolalert.database.Repositories.AlertDao;
import com.facecoolalert.database.entities.AlertLog;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.utils.PrefManager;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.util.ByteArrayDataSource;


public class EmailSender {
    public static void sendEmail(String subject, String message, String recipientEmail, AlertLog alertLog, AlertDao alertDao, Bitmap bmp, Subject recognizedSubject, Context context) {

        if(recipientEmail==null||recipientEmail.isEmpty())
            return;

        try {
            Properties properties = getProperties(context);
            properties.put("mail.debug", "false");



            System.out.println("Email properties gotten successfully");


            EmailSettings emailSettings = PrefManager.readEmailSettings(context);
            // Create a session with authentication
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailSettings.getEmail(), emailSettings.getPassword());
                }
            });



            try {
                MimeMessage mimeMessage=createMimeMessage(session,emailSettings,recipientEmail,subject,message,bmp,recognizedSubject.loadLocalImage(context));

                Transport.send(mimeMessage);

                // Email sent successfully, you can log this or perform other actions
                System.out.println("Email Sent Successfully");
            } catch (Exception e) {
                System.out.println("error Sending Email "+e);
                e.printStackTrace();
                // Handle email sending failure
            }
        }catch (Exception erd)
        {
            System.out.println("Email error "+erd);
            erd.printStackTrace();
        }
    }

    
    public static Properties getProperties(Context context)
    {
        EmailSettings emailSettings = PrefManager.readEmailSettings(context);

        System.out.println("getting Email properties "+emailSettings);
        // Create email properties
        Properties properties = new Properties();

        try {
            properties.put("mail.smtp.host", emailSettings.getSmtpHost());
            properties.put("mail.smtp.port", String.valueOf(emailSettings.getPort()));
            properties.put("mail.smtp.auth", "true");
        }catch (Exception ed)
        {

        }


        System.out.println("getting Email encryptions start");


        // Determine encryption based on the selected option
        String encryptionOption = emailSettings.getEncryption();
        switch (encryptionOption) {
            case "None (No Encryption)":
                // No encryption
                break;
            case "SSL (Secure Sockets Layer)":
                properties.put("mail.smtp.socketFactory.port", String.valueOf(emailSettings.getPort()));
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                properties.put("mail.smtp.auth", "true");
                break;
            case "TLS (Transport Layer Security)":
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.auth", "true");
                break;
            case "STARTTLS":
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.auth", "true");
                break;
            
        }
        System.out.println("getting Email encryption properties");


        String selectedService = emailSettings.getService();
        if ("Gmail".equalsIgnoreCase(selectedService)) {
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        } else if ("Yahoo Mail".equalsIgnoreCase(selectedService)) {
            properties.put("mail.smtp.host", "smtp.mail.yahoo.com");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.ssl.trust", "smtp.mail.yahoo.com");
        } else if ("Outlook".equalsIgnoreCase(selectedService)) {
            properties.put("mail.smtp.host", "smtp.live.com");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.ssl.trust", "smtp.live.com");
        } else if ("Apple Mail".equalsIgnoreCase(selectedService)) {
            properties.put("mail.smtp.host", "smtp.mail.me.com");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.ssl.trust", "smtp.mail.me.com");
        } else if ("Custom SMTP Server".equalsIgnoreCase(selectedService)) {
            properties.put("mail.smtp.host", emailSettings.getSmtpHost());
            properties.put("mail.smtp.port", String.valueOf(emailSettings.getPort()));

        }

        System.out.println("getting Email service properties");

        return properties;
    }


    private static byte[] getImageData(Bitmap bmp) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }




    public static MimeMessage createMimeMessage(Session session, EmailSettings emailSettings, String recipientEmail, String subject, String message, Bitmap bmp, Bitmap refImage) throws MessagingException {
        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(emailSettings.getEmail()));
            if(!recipientEmail.isEmpty())
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            mimeMessage.setSubject(subject);

            Multipart multipart=new MimeMultipart("mixed");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(message);

            // Add text part to the multipart
            multipart.addBodyPart(textPart);


            //add Recognized Image
            MimeBodyPart filePart=new MimeBodyPart();
            filePart.setFileName("Recognized Image.jpg");
            filePart.setDisposition(Part.ATTACHMENT);


            DataSource source = new ByteArrayDataSource(getImageData(bmp), "image/jpeg");
            filePart.setDataHandler(new DataHandler(source));

            multipart.addBodyPart(filePart);
            //end

            //add Reference Image
            MimeBodyPart reffilePart=new MimeBodyPart();
            reffilePart.setFileName("Reference Image.jpg");
            reffilePart.setDisposition(Part.ATTACHMENT);


            DataSource refsource = new ByteArrayDataSource(getImageData(refImage), "image/jpeg");
            reffilePart.setDataHandler(new DataHandler(refsource));

            multipart.addBodyPart(reffilePart);
            //end

            mimeMessage.setContent(multipart);

            return mimeMessage;
        } catch (Exception e) {
            e.printStackTrace();
            //throw new MessagingException("Error creating MimeMessage", e);
        }
        return null;
    }


    public static void sendEmail(String messageSubject, String message, List<String> emails, AlertLog alertLog, AlertDao alertDao, Bitmap bmp,Subject recognizedSubject, Context context) {
        try {
            Properties properties = getProperties(context);
            properties.put("mail.debug", "true");



            System.out.println("Email properties gotten successfully");


            EmailSettings emailSettings = PrefManager.readEmailSettings(context);
            // Create a session with authentication
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailSettings.getEmail(), emailSettings.getPassword());
                }
            });

            try {
                MimeMessage mimeMessage=createMimeMessage(session,emailSettings,"",messageSubject,message,bmp, recognizedSubject.loadLocalImage(context));

                InternetAddress[] internetAddresses = new InternetAddress[emails.size()];
                for (int i = 0; i < emails.size(); i++) {
                    internetAddresses[i] = new InternetAddress(emails.get(i));
                }

                mimeMessage.setRecipients(Message.RecipientType.TO, internetAddresses);
                Transport.send(mimeMessage);
                // Email sent successfully, you can log this or perform other actions
                System.out.println("Email Sent Successfully");
            } catch (Exception e) {
                Log.e("Email Sending",e.getMessage(),e);
                System.out.println("error Sending Email "+e);
                e.printStackTrace();
                // Handle email sending failure
            }
        }catch (Exception erd)
        {
            Log.e("Email Sending",erd.getMessage(),erd);
//            System.out.println("Email error "+erd);
            erd.printStackTrace();
        }
    }
}
