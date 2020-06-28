package com.facecoolalert.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;

import com.facecoolalert.R;

public class ImagePreviewDialog extends Dialog {

    private Bitmap bitmap;

    public ImagePreviewDialog(@NonNull Context context) {
        super(context);
    }

    public void show(Bitmap bitmap) {
        this.bitmap = bitmap;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_image_view);

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
    }
}
