package com.facecoolalert.database.entities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.facecoolalert.database.LongToStringMigration.LongToStringUidMigration;
import com.facecoolalert.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


@Entity(tableName = "Subject")
public class Subject {
    @NotNull
    @PrimaryKey(autoGenerate = false)
    private String uid;
    private String watchlist;
    private String firstName;
    private String lastName;
    private String ID;
    private String email;
    private String phone;
    private String address;

    @Ignore
    public Bitmap profilePhoto;


    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] features;

    private Double imageQuality;

    public String getImageFile()
    {
        return "subject"+getUid()+"photo.jpg";
    }


    public Subject() {
        this.uid = UUID.randomUUID().toString();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getWatchlist() {
        return watchlist;
    }

    public void setWatchlist(String watchlist) {
        this.watchlist = watchlist;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Bitmap getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(Bitmap profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public byte[] getFeatures() {
        return features;
    }

    public void setFeatures(byte[] features) {
        this.features = features;
    }

    public String getName() {
        return getFirstName() + " " + getLastName();
    }


    public Double getImageQuality() {
        if(imageQuality==null)
            return 0.0;
        else
            return imageQuality;
    }

    public void setImageQuality(Double imageQuality) {
        this.imageQuality = imageQuality;
    }



    public boolean saveImage(Context context, Bitmap bitmap) {
        this.profilePhoto=bitmap;
        try {
            FileOutputStream outputStream = new FileOutputStream(newImageSaveFile(context));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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


    public File newImageSaveFile(Context context) {
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

    public void deleteImage(Context context) {
        try {
            File file=newImageSaveFile(context);
            if (file.exists()) {
                if (file.delete()) {
                    this.profilePhoto = null;
                } else {
                }
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public Bitmap loadImage(Context context) {
        if(this.profilePhoto!=null)
            return this.profilePhoto;
        else
            return loadLocalImage(context);
    }

    public Bitmap loadLocalImage(Context context)
    {
        try {
            File file = newImageSaveFile(context);
            if (file.exists()) {
                this.profilePhoto=BitmapFactory.decodeFile(file.getAbsolutePath());
                return profilePhoto;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] loadImageBytes(Context context) {
        try {
            File file = newImageSaveFile(context);
            if (file.exists()) {
                // Decode the file into a Bitmap
                return FileUtils.readAllBytes(new FileInputStream(file));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // File not found or error occurred while loading
    }

    public boolean saveImageBytes(Context context,byte[] imageBytes) {
        try {
            File file = newImageSaveFile(context);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(imageBytes);
            outputStream.close();
            return true; // File saved successfully
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Error occurred while saving the file
        }
    }





}
