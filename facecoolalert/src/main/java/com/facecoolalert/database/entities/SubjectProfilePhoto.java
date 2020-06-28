package com.facecoolalert.database.entities;


import android.content.Context;
import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.facecoolalert.utils.converters.BitmapConverter;
import com.facecoolalert.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Entity
public class SubjectProfilePhoto {

    @NotNull
    @PrimaryKey(autoGenerate = false)
    private String uid;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] profilePhoto;

    public SubjectProfilePhoto(String uid) {
        this.uid=uid;
    }

    public String getImageFile()
    {
        return "subject"+getUid()+"photo.jpg";
    }


    public SubjectProfilePhoto() {
        this.uid = UUID.randomUUID().toString();
    }

    public SubjectProfilePhoto(String uid, byte[] profilePhoto) {
        this.uid=uid;
        this.profilePhoto = profilePhoto;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public byte[] getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(byte[] profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public Bitmap getBitmap()
    {
        return BitmapConverter.byteArrayToBitmap(getProfilePhoto());
    }

    public void setBitmap(Bitmap bmp)
    {
        setProfilePhoto(BitmapConverter.bitmapToByteArray(bmp));
    }


    public void loadFromLocal(Context context) {
        try {
            File file = newImageSaveFile(context);
            if (file.exists()) {
                // Decode the file into a Bitmap
                setProfilePhoto(FileUtils.readAllBytes(new FileInputStream(file)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean saveToLocal(Context context) {
        try {
            File file = newImageSaveFile(context);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(getProfilePhoto());
            outputStream.close();
            return true; // File saved successfully
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Error occurred while saving the file
        }
    }

    private File imageSaveFile(Context context) {
        String expLong=uid;
        if(expLong.contains("_"))
        {
            String[] parts=expLong.split("_");
            expLong=parts[parts.length-1];
        }
        String fileName="imagefile"+expLong+".jpg";
        // Get the app's private internal storage directory
        File internalStorageDir = context.getFilesDir();
        File OldestFile = new File(internalStorageDir, fileName);//earliest
        if(OldestFile.exists())
            return OldestFile;
        File newSaveFolder=new File(internalStorageDir,"subjectPhotos/");
        File olderFile=new File(newSaveFolder, "subject"+expLong+"photo.jpg");//before Long to stringuuid conversion

        return olderFile;
    }


    private File newImageSaveFile(Context context) {
        // Get the app's private internal storage directory
        File internalStorageDir = context.getFilesDir();
        File newSaveFolder=new File(internalStorageDir,"subjectPhotos/");
        if(!newSaveFolder.exists())
            newSaveFolder.mkdirs();
        File newLocation=new File(newSaveFolder, getImageFile());
        if(!newLocation.exists()){//if new location doesn`t have this file.
            File oldLocation=imageSaveFile(context);
            if(oldLocation.exists())//and old location has the file, we copy the image file to the new location.
                oldLocation.renameTo(newLocation);
        }
        return newLocation;
    }






}
