package com.facecoolalert.ui.subject.enrollments;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facecool.cameramanager.camera.BitmapUtils;
import com.facecoolalert.R;
import com.facecoolalert.common.imageSavePreview.SaveImagePreviewDialog;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.ui.camera.CameraFragment;
import com.facecoolalert.ui.subject.verification.VerificationActivity;
import com.facecoolalert.ui.subject.visits.SubjectVisits;
import com.facecoolalert.utils.RecognitionUtils;
import com.google.mlkit.vision.face.FaceDetector;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SubjectCard extends AppCompatActivity implements ProfileImagesProcessor{

    public SaveImagePreviewDialog imageSaveDialog;
    private byte[] features;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText idEditText;
    private EditText emailEditText;
    private EditText addressEditText;

    private FaceDetector faceDetector;

    private EditText phoneEditText;

    private Spinner watchlistSpinner;


    private Subject existingSubject;

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;

    private Bitmap myimage=null;
    private static final int REQUEST_IMAGE_PICK = 2;


    private TextView qualityTextView;

    private Double imageQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_card);


        View backButton=findViewById(R.id.backbutton);
        Button cancelButton=findViewById(R.id.cancel_button);


        qualityTextView=findViewById(R.id.qualityTextView);

        View.OnClickListener back= v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("subjectcard", "updated");
            setResult(RESULT_CANCELED, resultIntent);
            SubjectCard.this.finish();
        };

        backButton.setOnClickListener(back);
        cancelButton.setOnClickListener(back);


        imageView=findViewById(R.id.profile_picture);
        firstNameEditText = findViewById(R.id.first_name);
        lastNameEditText = findViewById(R.id.last_name);
        idEditText = findViewById(R.id.ID);
        emailEditText = findViewById(R.id.email);
        addressEditText = findViewById(R.id.adress);

        watchlistSpinner = findViewById(R.id.watchlist);
        phoneEditText = findViewById(R.id.phone_number);


        lookForIntents();

        verificationandvisits();
        changingofPicture();
        this.progressBar=findViewById(R.id.progress_user_image);
        handleCameraCapture();
        handleFileSelect();
        handleSaving();

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
            watchlistSpinner.setAdapter(adapter);
             new Handler(Looper.getMainLooper()).post(()-> {
                watchlistSpinner.setAdapter(adapter);
                 if(existingSubject!=null)
                     watchlistSpinner.setSelection(watchLists.indexOf(existingSubject.getWatchlist()));
            });

        }, AsyncTask.THREAD_POOL_EXECUTOR);

        configureImageSave();
    }

    private void configureImageSave() {
        imageView.setOnClickListener(v -> {
            if(myimage!=null) {
                SaveImagePreviewDialog saveImagePreviewDialog = new SaveImagePreviewDialog(getActivity());
                if (existingSubject != null)
                    saveImagePreviewDialog.show(myimage, existingSubject.getName() + " Profile Photo");
            }
        });
    }

    private void lookForIntents() {
        Intent myIntent = getIntent();
        if (myIntent != null) {
            if (myIntent.hasExtra("subject")) {
                String subjectId = myIntent.getStringExtra("subject"); // Provide a default value if "subject" extra is missing
                Log.d("Existing Subject", "existing subject  "+subjectId);
                if (subjectId != null&&!subjectId.isEmpty()) { // Check if the subjectId is valid (not the default value)
                    SubjectDao subjectDao = MyDatabase.getInstance(this).subjectDao();
                    CompletableFuture.supplyAsync(()->{
                        try{
                            return subjectDao.getSubjectByuid(subjectId);
                        }catch (Exception es){
                            Log.d("Existing Subject", "existing subject  "+es);
                            es.printStackTrace();
                            return null;
                        }
                    }).thenAcceptAsync(subject -> {
                        if(subject!=null) {
                            existingSubject = subject;
                            new Handler(Looper.getMainLooper()).post(this::loadExistingData);
                        }
                        else
                            Log.d("Existing Subject", "existing subject  Null");
                    });

                }
            }
        }
    }

    private void changingofPicture() {
        findViewById(R.id.editDp).setOnClickListener(v -> {
            v.setVisibility(View.INVISIBLE);
            findViewById(R.id.showDps).setVisibility(View.VISIBLE);
        });


    }


    private void loadExistingData() {
        if(existingSubject!=null) {
            firstNameEditText.setText(existingSubject.getFirstName());
            lastNameEditText.setText(existingSubject.getLastName());
            idEditText.setText(existingSubject.getID());
            addressEditText.setText(existingSubject.getAddress());
            emailEditText.setText(existingSubject.getEmail());

            phoneEditText.setText(existingSubject.getPhone());
            watchlistSpinner.setSelection(0);


            myimage=existingSubject.loadImage(this);
//            imageView.setImageBitmap(myimage);
            setNewProfilePhoto(existingSubject.getFeatures(),myimage,existingSubject.getImageQuality());

        }

    }




    private void verificationandvisits() {
        Button verificationButton=findViewById(R.id.verification_button);
        Button visitsButton=findViewById(R.id.visitsbutton);

        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent verificationintent=new Intent(getBaseContext(), VerificationActivity.class);
                if(existingSubject!=null)
                    verificationintent.putExtra("subject",existingSubject.getUid());
                startActivity(verificationintent);
            }
        });


        visitsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent visitsintent=new Intent(getBaseContext(), SubjectVisits.class);
                if(existingSubject!=null)
                    visitsintent.putExtra("subject",existingSubject.getUid());
                startActivity(visitsintent);
            }
        });

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
                if (ContextCompat.checkSelfPermission(SubjectCard.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SubjectCard.this,
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
                    Bitmap imageBitmap= com.facecool.cameramanager.utils.BitmapUtils.getPictureBitmap(imageCapturedLocation.getAbsolutePath());
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

    private ProgressBar progressBar;




//    private void openCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//    }

    private void handleSaving() {
        findViewById(R.id.submitbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myimage==null)
                {
                    Toast.makeText(getApplicationContext(), "You must Choose the Image", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(imageQuality<RecognitionUtils.enrollmentQualityThreshold&&imageQuality>0.0)
                {
                    Toast.makeText(SubjectCard.this, "Image Quality is below the Enrollment Threshold", Toast.LENGTH_SHORT).show();
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

                Subject mysubject=existingSubject;


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
                    return;
                }

                if(features!=null)
                    System.out.println("Not null saving");
                else {
                    //System.out.println("Saving null");
                    Toast.makeText(getApplicationContext(),"Face Data cannot be Empty",Toast.LENGTH_SHORT).show();
                    return;
                }



                SubjectDao subjectDao= MyDatabase.getInstance(getApplicationContext()).subjectDao();

                final android.os.Handler handler=new android.os.Handler();

                Subject finalMysubject = mysubject;
                new Thread()
                {
                    public void run()
                    {

                        AtomicReference<Boolean> willProceed= new AtomicReference<>(true);

                        int namesMatch=subjectDao.countByName(firstName,lastName);
                        int idsMatch=subjectDao.countByID(id);

                        if(existingSubject!=null) {
                            if (id.equals(existingSubject.getID().trim()))
                                idsMatch--;

                            if (finalMysubject.getName().equals(existingSubject.getName()))
                                namesMatch--;
                        }

                        if(idsMatch>0&&id.length()>0)
                        {
                            CountDownLatch latch = new CountDownLatch(1);
                            int finalIdsMatch = idsMatch;
                            new Handler(Looper.getMainLooper()).post(()->{
                                new AlertDialog.Builder(SubjectCard.this).
                                        setMessage("The Id You entered "+id+" has been Registered in "+ finalIdsMatch + " Other Subjects")
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


                        if(!willProceed.get())
                            return;


                        if(namesMatch>0&&finalMysubject.getName().length()>0)
                        {
                            CountDownLatch latch = new CountDownLatch(1);
                            int finalNamesMatch = namesMatch;
                            new Handler(Looper.getMainLooper()).post(()->{
                                new AlertDialog.Builder(SubjectCard.this).
                                        setMessage("The Name You entered "+finalMysubject.getName()+" has been Registered in "+ finalNamesMatch + " Other Subjects")
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
                        if(!willProceed.get())
                            return;

                        subjectDao.updateSubject(finalMysubject);
                        CameraFragment.lastSubjectsChange++;

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(finalMysubject.saveImage(getApplicationContext(),myimage))
                                {
                                    RecognitionUtils.refreshSubjects();
                                    Toast.makeText(getApplicationContext(), "Saved SuccessFully", Toast.LENGTH_SHORT).show();
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("subjectcard", "updated");
                                    setResult(RESULT_OK, resultIntent);
                                    SubjectCard.this.finish();

                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Error Saving", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

                    }
                }.start();

            }
        });
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

    @Override
    public Activity getActivity() {
        return SubjectCard.this;
    }
}