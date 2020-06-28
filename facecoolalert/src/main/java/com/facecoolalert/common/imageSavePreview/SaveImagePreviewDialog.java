package com.facecoolalert.common.imageSavePreview;



import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;

import com.facecoolalert.R;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.subject.enrollments.EnrollSubject;
import com.facecoolalert.ui.subject.enrollments.ProfileImagesProcessor;
import com.facecoolalert.ui.subject.enrollments.SubjectCard;
import com.facecoolalert.ui.visits.VisitsFragment;
import com.facecoolalert.utils.converters.BitmapConverter;
import com.google.android.material.button.MaterialButton;

import java.io.FileOutputStream;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


public class SaveImagePreviewDialog extends Dialog {

    public static final int SAVE_IMAGE_REQUEST_CODE = 149;
    private Activity activity;
    private Bitmap bitmap;

    private String title;




    public SaveImagePreviewDialog(@NonNull Context context) {
        super(context);
    }

    public SaveImagePreviewDialog(Activity activity) {
        super(activity);
        this.activity=activity;

    }

    public void show(Bitmap bitmap,String title) {
        this.bitmap = bitmap;
        this.title=title;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setTitle(title);
        setContentView(R.layout.dialog_image_save_preview);

        if(bitmap==null)
            return;

        ImageView imageViewPreview = findViewById(R.id.imageViewPreview);


//        // Calculate the desired width based on the screen width
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;

//        ImagePreviewDialog.this.getWindow().setW
        // Set the width of the dialog
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());


        // Calculate the desired height based on the bitmap's aspect ratio
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        int btnAllowance=100;

        int desiredWidth;
        int desiredHeight;

        desiredWidth = screenWidth;
        desiredHeight = (int) (desiredWidth * ((float) bitmapHeight / bitmapWidth));

        if(desiredHeight>screenHeight-btnAllowance)
        {
            desiredHeight=screenHeight-btnAllowance;
            desiredWidth=(int) (desiredHeight * ((float) bitmapWidth / bitmapHeight));
        }

        // Set the calculated width and height to the ImageView
        imageViewPreview.getLayoutParams().width = desiredWidth;
        imageViewPreview.getLayoutParams().height = desiredHeight;
        imageViewPreview.requestLayout();  // Force layout update

        // Set the scaled bitmap to the ImageView
//        imageViewPreview.setImageBitmap(Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, true));
        imageViewPreview.setImageBitmap(bitmap);


        layoutParams.width = desiredWidth;
        layoutParams.height = desiredHeight+btnAllowance;
        getWindow().setAttributes(layoutParams);


        Button closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(); // Close the dialog
            }
        });

        MaterialButton saveImage=findViewById(R.id.saveImage);
        saveImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_TITLE, title);
//            chooseSaveLocation.launch(intent);

            if(activity instanceof MainActivity) {
                ((MainActivity) activity).imageSaveDialog = this;
                activity.startActivityForResult(intent, SAVE_IMAGE_REQUEST_CODE);
            } else if (activity instanceof SubjectCard) {
                ((SubjectCard) activity).imageSaveDialog = this;
                activity.startActivityForResult(intent, SAVE_IMAGE_REQUEST_CODE);
            } else if (activity instanceof EnrollSubject) {
                ((EnrollSubject) activity).imageSaveDialog = this;
                activity.startActivityForResult(intent, SAVE_IMAGE_REQUEST_CODE);
            }
        });
    }


//    // You need to create a launcher variable inside onAttach or onCreate or global, i.e, before the activity is displayed
//    ActivityResultLauncher<Intent> chooseSaveLocation;


    public void saveImageFile(Uri uri)
    {
        if(uri!=null)
        {
            try {
                ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(uri, "w");
                if( pfd != null ) {
                    FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    fileOutputStream.write(BitmapConverter.bitmapToByteArray(bitmap));
                    fileOutputStream.close();
                    Toast.makeText(getContext(), "Saved Successfully", Toast.LENGTH_SHORT).show();

                }
            }
            catch(Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error Saving : "+e, Toast.LENGTH_SHORT).show();
            }
        }
    }




}
