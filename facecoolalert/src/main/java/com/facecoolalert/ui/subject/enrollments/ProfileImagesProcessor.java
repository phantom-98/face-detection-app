package com.facecoolalert.ui.subject.enrollments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.facecool.attendance.facedetector.FaceData;
import com.facecoolalert.ui.detect.FaceDetectorProcessor;
import com.facecoolalert.utils.RecognitionUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface ProfileImagesProcessor {



    Context getBaseContext();

  




    default void processImage(Bitmap imageBitmap) {
        getProgressBar().setVisibility(View.VISIBLE);

        FaceDetectorProcessor faceDetectorProcessor = new FaceDetectorProcessor(getBaseContext(), new FaceDetectorProcessor.ResultListener() {
            @Override
            public void onSuccess(FaceData faceData, boolean bOnlyReg) {
                //Face face=faceData.getFace();

                if (faceData.getFeatures() == null) {
                    getProgressBar().setVisibility(View.GONE);
                    Toast.makeText(getBaseContext(), "Unable to extract features from the image", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (faceData.getScoreMatch() > RecognitionUtils.similarityThreshold) {
                    //Toast.makeText(getBaseContext(), "Face Already Enrolled", Toast.LENGTH_SHORT).show();
                    //return;
                    new AlertDialog.Builder(getActivity()).setMessage("This Face had already been enrolled as " + faceData.getName() + ". Do you still want to Use it ?")
                            .setNegativeButton("No", (dialog, i) -> {
                                getProgressBar().setVisibility(View.GONE);
                                return;
                            })
                            .setPositiveButton("Continue", ((dialogInterface, i) -> {
                                getProgressBar().setVisibility(View.GONE);
                                setNewProfilePhoto(faceData);
                            })).create().show();
                } else {
                    getProgressBar().setVisibility(View.GONE);
                    setNewProfilePhoto(faceData);
                }

            }

            @Override
            public void onSuccessNull() {
                getProgressBar().setVisibility(View.GONE);
                Toast.makeText(getBaseContext(), "No Faces Found", Toast.LENGTH_SHORT).show();
                return;
            }

            @Override
            public void onError(Exception exp) {
               getProgressBar().setVisibility(View.GONE);
                Toast.makeText(getBaseContext(), "Error : Unable to get Face Features", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        faceDetectorProcessor.isReportSuccessNull = true;

        faceDetectorProcessor.saveToDb = false;
        faceDetectorProcessor.detectFace(imageBitmap);
    }

    Activity getActivity();

    ProgressBar getProgressBar();

    default void setNewProfilePhoto(byte[] features, Bitmap myimage, Double imageQuality) {
        Log.d(" Recognition Result", "setNewProfilePhoto: "+myimage);
        setImageQuality(imageQuality);
        if(imageQuality>=RecognitionUtils.enrollmentQualityThreshold||imageQuality==0.0)
        {
            setMyImage(myimage);
            setFeatures(features);
            new Handler(Looper.getMainLooper()).post(()-> {
                getImageView().setImageBitmap(getMyImage());
                getQualityTextView().setVisibility(View.VISIBLE);
                getQualityTextView().setTextColor(Color.BLACK);
                getQualityTextView().setText(String.format("Quality : %.1f",imageQuality));
            });
        }
        else {
            new Handler(Looper.getMainLooper()).post(()-> {
                setMyImage(myimage);
                getImageView().setImageBitmap(myimage);
                getQualityTextView().setVisibility(View.VISIBLE);
                getQualityTextView().setTextColor(Color.RED);
                getQualityTextView().setText(String.format("Quality : %.1f",imageQuality));
                Toast.makeText(getBaseContext(), "Image Quality is below the Enrollment Threshold", Toast.LENGTH_SHORT).show();
            });
        }
    }

    TextView getQualityTextView();

    Bitmap getMyImage();

    ImageView getImageView();

    void setFeatures(byte[] features);

    void setMyImage(Bitmap myimage);

    void setImageQuality(Double imageQuality);

    default void setNewProfilePhoto(FaceData next) {
//        myimage = next.getBmp();
//        imageView.setImageBitmap(myimage);
//        features = next.getFeatures();
        setNewProfilePhoto(next.getFeatures(),next.getBestImage(),next.getImageQualityScore());
    }


    default void openCamera()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.facecoolalert.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                getActivity().startActivityForResult(takePictureIntent, EnrollSubject.REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    default File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        setImageCapturedLocation(image);
        return image;
    }

    void setImageCapturedLocation(File image);
}
