package com.facecoolalert.ui.subject.verification;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.FaceData;
import com.facecool.attendance.facedetector.FaceEngine;
import com.facecool.cameramanager.utils.BitmapUtils;
import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.utils.FileUtils;
import com.facecoolalert.utils.RecognitionUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class VerificationActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 2;
    private Subject subjectToVerify;

    private ImageView current_upload;

    private TextView upload_file;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);


        View backButton=findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent verificationIntent=getIntent();
        if(verificationIntent.hasExtra("subject"))
        {
            new Thread(()-> {
                feedData(verificationIntent.getStringExtra("subject"));
            }).start();
        }


        current_upload=findViewById(R.id.upload_photo);
        progressBar=findViewById(R.id.progress);
        upload_file=findViewById(R.id.upload_name);
        handleFaceUpload();

    }

    @SuppressLint("SetTextI18n")
    private void feedData(String subject) {
        SubjectDao subjectDao= MyDatabase.getInstance(this).subjectDao();
        try{
            subjectToVerify=subjectDao.getSubjectByuid(subject);
            if(subjectToVerify!=null)
            {
                TextView name=findViewById(R.id.profile_name);
                ImageView currentProfile=findViewById(R.id.current_profile);
                name.setText("Name : "+subjectToVerify.getName());
                currentProfile.setImageBitmap(subjectToVerify.loadImage(this));

            }

        }catch (Exception ed)
        {
            ed.printStackTrace();
        }
    }

    private void handleFaceUpload() {
        findViewById(R.id.upload_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            try {
                upload_file.setText("File:" + FileUtils.getFriendlyPathFromUri(this, selectedImageUri));
            }catch (Exception es)
            {
                upload_file.setText(selectedImageUri.getLastPathSegment());
            }
            try {
                Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(this.getContentResolver(),selectedImageUri);
                //imageView.setImageBitmap(imageBitmap);
                // Process the selected image
                processImage(imageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processImage(Bitmap imageBitmap) {

        current_upload.setImageBitmap(imageBitmap);
        progressBar.setVisibility(View.VISIBLE);
        verify(imageBitmap);


    }

    private void verify(Bitmap imageBitmap) {

        int landmarkMode = FaceDetectorOptions.LANDMARK_MODE_ALL;//.LANDMARK_MODE_NONE;
        int performanceMode = FaceDetectorOptions.PERFORMANCE_MODE_FAST;//.PERFORMANCE_MODE_ACCURATE;
        boolean bMoreAccurate = Constants.getFaceMoreAccurate(this);
        if( bMoreAccurate )
            performanceMode = FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE;
        float minFaceSize = Constants.getMinFaceSize(this);

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(performanceMode)
                        .setLandmarkMode(landmarkMode)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                        .setMinFaceSize(minFaceSize)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        FaceDetector faceDetector = FaceDetection.getClient(options);


        InputImage image = InputImage.fromBitmap(imageBitmap, 0);


        faceDetector
                .process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(@NonNull List<Face> faces) {
                        if(faces.size()==0) {
                            setScore(0);
                            Toast.makeText(getBaseContext(),"No Faces Found",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FaceData highest=null;
                        Bitmap myimage=null;
                        for (Face face : faces) {
                            FaceData uploaded= RecognitionUtils.extractFeatures(imageBitmap, RecognitionUtils.createFaceData(imageBitmap, face));

                            if(uploaded.getFeatures()==null)
                            {
                                setScore(0);
                                Toast.makeText(getBaseContext(),"Unable to get Features",Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if(subjectToVerify.getFeatures()==null)
                            {
                                setScore(0);
                                Toast.makeText(getBaseContext(),"Subject  Features cannot be found",Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Rect boundingBox = uploaded.getFace().getBoundingBox();
                            myimage=Bitmap.createBitmap(imageBitmap,boundingBox.left,
                                    boundingBox.top,
                                    boundingBox.width(),
                                    boundingBox.height());





                            float similarity=FaceEngine.getSimilarity(subjectToVerify.getFeatures(),uploaded.getFeatures())*100f;
                            if(similarity>RecognitionUtils.similarityThreshold){
                                setScore(similarity);



                                current_upload.setImageBitmap(myimage);
                                return;
                            }
                            uploaded.setScoreMatch(similarity);
                            if(highest==null)
                                highest=uploaded;
                            else if(uploaded.getScoreMatch()>highest.getScoreMatch())
                                highest=uploaded;
                        }
                        setScore(highest.getScoreMatch());
                        current_upload.setImageBitmap(myimage!=null?myimage:highest.getBmp());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        setScore(0);
                        Toast.makeText(getBaseContext(),"An Error Occurred",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
    }

    public void setScore(float score)
    {
        progressBar.setVisibility(GONE);
        TextView status=findViewById(R.id.status);
        TextView scoretext=findViewById(R.id.score);
        float rounded=Math.round(score*1000f)/1000f;
        scoretext.setText(rounded+"");
        if(score< RecognitionUtils.similarityThreshold)
        {
            status.setText("Failed");
            status.setTextColor(Color.RED);
            scoretext.setTextColor(Color.RED);
        }
        else{
            status.setText("Positive Match!");
            status.setTextColor(Color.GREEN);
            scoretext.setTextColor(Color.GREEN);
        }
    }


}