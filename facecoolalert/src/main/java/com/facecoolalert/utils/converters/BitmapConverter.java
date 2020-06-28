package com.facecoolalert.utils.converters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.room.TypeConverter;
import java.io.ByteArrayOutputStream;

public class BitmapConverter {

    @TypeConverter
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    @TypeConverter
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        if(byteArray!=null)
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        else
            return null;
    }
}
