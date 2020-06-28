package com.facecool.utils

import android.R.id.message
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.ContextCompat.checkSelfPermission
import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Part
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource


internal object Notify{
    fun sendEmail(
        emailService: Int,
        yourMail: String,
        pwd: String,
        subject: String,
        message: String,
        recipientEmail: String,
        bmp: Bitmap
    ) {
        if (!yourMail.isEmail() || !recipientEmail.isEmail()) return
        try {
            val properties = getProperties(emailService)
            properties["mail.debug"] = "false"

            val session: Session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        yourMail,
                        pwd
                    )
                }
            })
            val mimeMessage: MimeMessage? = createMimeMessage(
                session, yourMail, recipientEmail, subject, message,
                bmp
            )
            Transport.send(mimeMessage)

            Log.d("Email Test","Email Sent Successfully")
        } catch (e: java.lang.Exception) {
            Log.d("Email Test", "Email error $e")
        }
    }
    fun getProperties(service: Int): Properties {
        val properties = Properties()
        val services = listOf<String>("Gmail", "Yahoo", "Outlook", "Apple")
        val preferedService = services[service]
        if ("Gmail".equals(preferedService, ignoreCase = true)) {
            properties.put("mail.smtp.host", "smtp.gmail.com")
            properties.put("mail.smtp.port", "587")
            properties.put("mail.smtp.starttls.enable", "true")
            properties.put("mail.smtp.auth", "true")
            properties.put("mail.smtp.ssl.trust", "smtp.gmail.com")
        } else if ("Yahoo Mail".equals(preferedService, ignoreCase = true)) {
            properties.put("mail.smtp.host", "smtp.mail.yahoo.com")
            properties.put("mail.smtp.port", "587")
            properties.put("mail.smtp.starttls.enable", "true")
            properties.put("mail.smtp.auth", "true")
            properties.put("mail.smtp.ssl.trust", "smtp.mail.yahoo.com")
        } else if ("Outlook".equals(preferedService, ignoreCase = true)) {
            properties.put("mail.smtp.host", "smtp.live.com")
            properties.put("mail.smtp.port", "587")
            properties.put("mail.smtp.starttls.enable", "true")
            properties.put("mail.smtp.auth", "true")
            properties.put("mail.smtp.ssl.trust", "smtp.live.com")
        } else if ("Apple Mail".equals(preferedService, ignoreCase = true)) {
            properties.put("mail.smtp.host", "smtp.mail.me.com")
            properties.put("mail.smtp.port", "587")
            properties.put("mail.smtp.starttls.enable", "true")
            properties.put("mail.smtp.auth", "true")
            properties.put("mail.smtp.ssl.trust", "smtp.mail.me.com")
        }
        
        return properties
    }
    
    private fun getImageData(bmp: Bitmap): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
    
    fun createMimeMessage(
        session: Session?,
        yourMail: String,
        recipientEmail: String,
        subject: String?,
        message: String?,
        bmp: Bitmap
    ): MimeMessage? {
        try {
            Thread.currentThread().setContextClassLoader( ClassLoader.getSystemClassLoader() )
            val mimeMessage = MimeMessage(session)
            mimeMessage.setFrom(InternetAddress(yourMail))
            if (!recipientEmail.isEmpty()) mimeMessage.setRecipient(
                Message.RecipientType.TO,
                InternetAddress(recipientEmail)
            )
            mimeMessage.setSubject(subject)
            val multipart: Multipart = MimeMultipart()
            // Add text part to the multipart
            val textPart = MimeBodyPart()
            textPart.setText(message)
            multipart.addBodyPart(textPart)
            
            //add Recognized Image
            val filePart = MimeBodyPart()
            filePart.setFileName("Recognized Face.jpg")
            filePart.setDisposition(Part.ATTACHMENT)
            val source: DataSource = ByteArrayDataSource(getImageData(bmp), "image/jpeg")
            filePart.setDataHandler(DataHandler(source))
            multipart.addBodyPart(filePart)
            
            //end
            mimeMessage.setContent(multipart)
            return mimeMessage
        } catch (e: Exception) {
            Log.d("Email Test", "error: ${e}")
        }
        return null
    }

    fun sendSMS(msg: String, phone: String, context: Context) {
        try {
            if (checkSelfPermission(context, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            if (subscriptionManager != null) {
                for (info in subscriptionManager.activeSubscriptionInfoList) {
                    try {
                        var smsManager = context.getSystemService(SmsManager::class.java)
                        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.S)
                            smsManager = smsManager.createForSubscriptionId(info.subscriptionId)
                        else smsManager = SmsManager.getSmsManagerForSubscriptionId(info.subscriptionId)
                        if (smsManager != null) {
                            smsManager.sendTextMessage(phone, null, msg, null, null)
                            return
                        } else {
                            Log.e(
                                "SMS Sender",
                                "Failed to get SmsManager for subscription ID: " + info.subscriptionId
                            )
                        }
                    } catch (_: Exception){

                    }
                }
            } else {
                Log.e("MMS Sender", "SubscriptionManager is null")
            }
        } catch (e: java.lang.Exception) {
            Log.d("SMS Test", "error : ${e.message}")
        }
    }
}