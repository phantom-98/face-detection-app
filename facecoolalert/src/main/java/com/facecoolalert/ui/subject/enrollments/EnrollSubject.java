package com.facecoolalert.ui.subject.enrollments;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facecool.attendance.facedetector.FaceData;
import com.facecool.cameramanager.utils.BitmapUtils;

import com.facecoolalert.R;
import com.facecoolalert.common.imageSavePreview.SaveImagePreviewDialog;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.ui.camera.CameraFragment;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.utils.RecognitionUtils;
import com.google.mlkit.vision.face.FaceDetector;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class EnrollSubject extends AppCompatActivity implements ProfileImagesProcessor {

    public SaveImagePreviewDialog imageSaveDialog;
    public static FaceData next;
    private byte[] features;


    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText idEditText;
    private EditText emailEditText;
    private EditText addressEditText;

    private FaceDetector faceDetector;

    private EditText phoneEditText;

    private Spinner watchlistSpinner;

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;

    private Bitmap myimage=null;
    private static final int REQUEST_IMAGE_PICK = 2;
    private ProgressBar progressBar;

    private Double imageQuality;

    private TextView qualityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_subject);

        View backButton=findViewById(R.id.backbutton);
        Button cancelButton=findViewById(R.id.cancel_button);

        qualityTextView=findViewById(R.id.qualityTextView);
        qualityTextView.setVisibility(View.GONE);


        View.OnClickListener back=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("subjectcard", "updated");
                setResult(RESULT_CANCELED, resultIntent);
                EnrollSubject.this.finish();
            }
        };

        backButton.setOnClickListener(back);
        cancelButton.setOnClickListener(back);


        handleSaving();


        firstNameEditText = findViewById(R.id.first_name);
        lastNameEditText = findViewById(R.id.last_name);
        idEditText = findViewById(R.id.ID);
        emailEditText = findViewById(R.id.email);
        addressEditText = findViewById(R.id.adress);

        watchlistSpinner = findViewById(R.id.watchlist);
        phoneEditText = findViewById(R.id.phone_number);


        imageView=findViewById(R.id.profile_picture);
        handleCameraCapture();
        handleFileSelect();

        this.progressBar=findViewById(R.id.progress_user_image);


        lookForIntents();

        WatchlistDao watchlistDao=MyDatabase.getInstance(getBaseContext()).watchlistDao();

        CompletableFuture.supplyAsync(()->{//feed watchlist data
            try{
                return watchlistDao.listAll();
            }catch (Exception es)
            {
                return Collections.emptyList();
            }
        }).thenAcceptAsync(watchLists->{
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, (List<String>) watchLists);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            new Handler(Looper.getMainLooper()).post(()-> {
                watchlistSpinner.setAdapter(adapter);
            });
        }, AsyncTask.THREAD_POOL_EXECUTOR);

        configureImageSave();
    }


    private void configureImageSave() {
        imageView.setOnClickListener(v -> {
            if(myimage!=null) {
                SaveImagePreviewDialog saveImagePreviewDialog = new SaveImagePreviewDialog(getActivity());
                saveImagePreviewDialog.show(myimage, "New Profile Photo");
            }
        });
    }

    private void lookForIntents() {
        Intent intent=getIntent();
        if(intent.hasExtra("RecognitionResult"))
        {

            String resuliId=intent.getStringExtra("RecognitionResult");
            Log.d("Recognition Result", "has Extras "+resuliId);
            if(resuliId.equals("next"))//when user clicks on active photo of live camera
            {
                if(next!=null&&next.getFeatures()!=null) {
                    //check if the face features had already been enrolled
                    FaceData tmp=new FaceData(null,1,null);
                    tmp.setFeatures(next.getFeatures());
                    RecognitionUtils.recognizeFace(tmp);
                    if(tmp.getScoreMatch()<RecognitionUtils.similarityThreshold) {//if not already enrolled
                        setNewProfilePhoto(next);
                    }
                    else{//if features had already been enrolled.
                        Log.d("Recognition Result", "features enrolled already ");
                        try {
                            new AlertDialog.Builder(getActivity()).setMessage("This Face has already been enrolled as " + tmp.getName() + ". Do you still want to enroll it again ?")
                                    .setNegativeButton("No", (dialog, i) -> {
                                    })
                                    .setPositiveButton("Continue", ((dialogInterface, i) -> {
                                        setNewProfilePhoto(next);
                                    }
                                    )).create().show();
                        }catch (Exception es)
                        {
                            es.printStackTrace();
                        }
                    }
                }
            }
            else {//Enrollment from history(Recognition Result
                RecognitionResultDao recognitionResultDao = MyDatabase.getInstance(this).recognitionResultDao();
                CompletableFuture.supplyAsync(()->{
                    try{
                        return recognitionResultDao.getByUid(resuliId);
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                        return null;
                    }
                }).thenAcceptAsync(recognitionResult -> {
                    Log.d("Recognition Result", "gotten Result ");
                    if(recognitionResult!=null) {
                        FaceData ref = recognitionResult.genFaceData();
                        //check if the face features had already been enrolled
                        FaceData tmp = null;
                        tmp = new FaceData(null, 1, null);
                        try {
                            tmp.setFeatures(ref.getFeatures());
                            RecognitionUtils.recognizeFace(tmp);
                        } catch (Exception es) {
                            es.printStackTrace();
                        }
                        Log.d("Recognition Result", "gotten Result " + tmp.getScoreMatch());
                        if (tmp.getScoreMatch() < RecognitionUtils.similarityThreshold) {//if not already enrolled
                            setNewProfilePhoto(ref);
                        } else {//if features had already been enrolled.
                            Log.d("Recognition Result", "features enrolled already ");
                            FaceData finalTmp = tmp;
                            new Handler(Looper.getMainLooper()).post(() -> {
                                new AlertDialog.Builder(EnrollSubject.this).setMessage("This Face has already been enrolled as " + finalTmp.getName() + ". Do you still want to enroll it again ?")
                                        .setNegativeButton("No", (dialog, i) -> {
                                        })
                                        .setPositiveButton("Continue", ((dialogInterface, i) -> {
                                            setNewProfilePhoto(ref);
                                        }
                                        )).create().show();
                            });
                        }
                    } else
                        Log.d("Recognition Result", "gotten Null "+resuliId);
                },AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }




    private void handleFileSelect() {
        ((ImageView)findViewById(R.id.file_choose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });
    }

    private void handleCameraCapture() {

        ((ImageView) findViewById(R.id.camera_choose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EnrollSubject.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EnrollSubject.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    openCamera();
                }
            }
        });
    }

    private File imageCapturedLocation=null;

    public void setImageCapturedLocation(File imageCapturedLocation) {
        this.imageCapturedLocation = imageCapturedLocation;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                if(imageCapturedLocation!=null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    Bitmap imageBitmap=BitmapUtils.getPictureBitmap(imageCapturedLocation.getAbsolutePath());
                    if(imageBitmap!=null)
                        processImage(imageBitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(this.getContentResolver(),selectedImageUri);
                //imageView.setImageBitmap(imageBitmap);

                // Process the selected image
                processImage(imageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(requestCode == SaveImagePreviewDialog.SAVE_IMAGE_REQUEST_CODE&&resultCode==RESULT_OK)
        {
            if(imageSaveDialog!=null)
                imageSaveDialog.saveImageFile(data.getData());
        }

    }



//    private void openCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//    }

    private void handleSaving() {
        findViewById(R.id.submitbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                if(myimage==null)
                {
                    Toast.makeText(getApplicationContext(), "You must Choose the Image", Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);
                    return;
                }
                if(imageQuality<RecognitionUtils.enrollmentQualityThreshold&&imageQuality>0.0)
                {
                    Toast.makeText(EnrollSubject.this, "Image Quality is below the Enrollment Threshold", Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);
                    return;
                }

// Get the text from the EditText fields

                String watchlist= watchlistSpinner.getSelectedItem().toString();
                String firstName = firstNameEditText.getText().toString().trim();
                String lastName = lastNameEditText.getText().toString().trim();
                String id = idEditText.getText().toString().trim();
                String email = emailEditText.getText().toString();
                String phone=phoneEditText.getText().toString();
                String address = addressEditText.getText().toString();

// Now you have the text from the EditText fields in the respective variables (firstName, lastName, id, email, and address)

                Subject mysubject=new Subject();
                mysubject.setWatchlist(watchlist);
                mysubject.setAddress(address);
                mysubject.setEmail(email);
                mysubject.setFirstName(firstName);
                mysubject.setLastName(lastName);
                mysubject.setID(id);
                mysubject.setFeatures(features);
                mysubject.setPhone(phone);
                mysubject.setImageQuality(imageQuality);

                if(mysubject.getName().length()<2)
                {
                    Toast.makeText(getApplicationContext(), "Please Enter Subject Name", Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);
                    return;
                }

                if(features!=null)
                    System.out.println("Not null saving");
                else {
                    //System.out.println("Saving null");
                    Toast.makeText(getApplicationContext(),"Face Data cannot be Empty",Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);
                    return;
                }

                SubjectDao subjectDao= MyDatabase.getInstance(getApplicationContext()).subjectDao();

                final android.os.Handler handler=new android.os.Handler(Looper.getMainLooper());

                Subject finalMysubject = mysubject;
                new Thread()
                {
                    public void run()
                    {

                            AtomicReference<Boolean> willProceed= new AtomicReference<>(true);

                            int namesMatch=subjectDao.countByName(firstName,lastName);
                            int idsMatch=subjectDao.countByID(id);

                            if(idsMatch>0&&id.length()>0)
                            {
                                CountDownLatch latch = new CountDownLatch(1);
                                new Handler(Looper.getMainLooper()).post(()->{
                                new AlertDialog.Builder(EnrollSubject.this).
                                        setMessage("The Id You entered "+id+" has been Registered in "+idsMatch + " Other Subjects")
                                        .setNegativeButton("Change", (dialog, i)->{
                                            willProceed.set(false);
                                            latch.countDown();
                                            return;
                                        })
                                        .setPositiveButton("Continue To Use", ((dialogInterface, i) -> {
                                            willProceed.set(true);
                                            latch.countDown();
                                        })).create().show();
                                });
                                try {
                                    // Wait until the tasks are complete or until a timeout
                                    latch.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }


                        if(!willProceed.get()) {
                            new Handler(Looper.getMainLooper()).post(()->{v.setEnabled(true);});
                            return;
                        }

                        if(namesMatch>0&&finalMysubject.getName().length()>0)
                        {
                            CountDownLatch latch = new CountDownLatch(1);
                            new Handler(Looper.getMainLooper()).post(()->{
                                new AlertDialog.Builder(EnrollSubject.this).
                                        setMessage("The Name You entered "+finalMysubject.getName()+" has been Registered in "+namesMatch + " Other Subjects")
                                        .setNegativeButton("Change", (dialog, i)->{
                                            willProceed.set(false);
                                            latch.countDown();
                                            return;
                                        })
                                        .setPositiveButton("Continue To Use", ((dialogInterface, i) -> {
                                            willProceed.set(true);
                                            latch.countDown();
                                        })).create().show();
                            });
                            try {
                                // Wait until the tasks are complete or until a timeout
                                latch.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if(!willProceed.get()){
                            new Handler(Looper.getMainLooper()).post(()->{v.setEnabled(true);});
                            return;
                        }


                            subjectDao.insertSubject(finalMysubject);

                            RecognitionUtils.subjects.add(finalMysubject);
                            CameraFragment.lastSubjectsChange++;

                        final Boolean hasSavedSubjectPhoto=finalMysubject.saveImage(getApplicationContext(),myimage);
                        if(hasSavedSubjectPhoto)
                            RecognitionUtils.refreshSubjects();
                        handler.post(() -> {
                            if(hasSavedSubjectPhoto)
                            {
                                Toast.makeText(getApplicationContext(), "Saved SuccessFully", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("subjectcard", "updated");
                                setResult(RESULT_OK, resultIntent);
                                EnrollSubject.this.finish();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error Saving", Toast.LENGTH_SHORT).show();
                                v.setEnabled(true);
                            }
                        });

                    }
                }.start();

            }
        });
    }

    @Override
    public Activity getActivity() {
        return EnrollSubject.this;
    }

    @Override
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public TextView getQualityTextView() {
        return qualityTextView;
    }

    @Override
    public Bitmap getMyImage() {
        return myimage;
    }

    @Override
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public void setFeatures(byte[] features) {
        this.features=features;
    }

    @Override
    public void setMyImage(Bitmap myimage) {
        this.myimage=myimage;
    }

    @Override
    public void setImageQuality(Double imageQuality) {
        this.imageQuality=imageQuality;
    }
}