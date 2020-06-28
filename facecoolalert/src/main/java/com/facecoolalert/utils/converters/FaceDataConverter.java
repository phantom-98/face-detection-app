package com.facecoolalert.utils.converters;

import androidx.room.TypeConverter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.facecool.attendance.facedetector.FaceData;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class FaceDataConverter {

    @TypeConverter
    public static FaceData fromByteArray(byte[] data) {
        if (data == null) {
            return null;
        }

        int position = 0;

        // Deserialize the Bitmap bmp
        int bmpSize = ByteBuffer.wrap(data, position, 4).getInt();
        position += 4;
        byte[] bmpBytes = new byte[bmpSize];
        System.arraycopy(data, position, bmpBytes, 0, bmpSize);
        position += bmpSize;
        Bitmap bmp = BitmapFactory.decodeByteArray(bmpBytes, 0, bmpSize);

        // Deserialize the float fLive
        float fLive = ByteBuffer.wrap(data, position, 4).getFloat();
        position += 4;

        // Deserialize the byte[] features
        int featuresSize = ByteBuffer.wrap(data, position, 4).getInt();
        position += 4;
        byte[] features = new byte[featuresSize];
        System.arraycopy(data, position, features, 0, featuresSize);
        position += featuresSize;

        // Deserialize the String name
        int nameSize = ByteBuffer.wrap(data, position, 4).getInt();
        position += 4;
        byte[] nameBytes = new byte[nameSize];
        System.arraycopy(data, position, nameBytes, 0, nameSize);
        String name = new String(nameBytes);
        position += nameSize;

        // Deserialize the float scoreMatch
        float scoreMatch = ByteBuffer.wrap(data, position, 4).getFloat();

        FaceData faceData=new FaceData(null,fLive,bmp);
        faceData.setName(name);
        faceData.setScoreMatch(scoreMatch);
        faceData.setFeatures(features);


        return faceData;
    }

    @TypeConverter
    public static byte[] toByteArray(FaceData faceData) {
        if (faceData == null) {
            return null;
        }

        int totalSize = 0;

        // Calculate the total size
        totalSize += 4; // 4 bytes for float fLive
        byte[] features = faceData.getFeatures();
        totalSize += 4 + features.length; // 4 bytes for size
        Bitmap bmp = faceData.getBmp();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bmpBytes = stream.toByteArray();
        totalSize += 4 + bmpBytes.length; // 4 bytes for size
        byte[] nameBytes = faceData.getName().getBytes();
        totalSize += 4 + nameBytes.length; // 4 bytes for size
        totalSize += 4; // 4 bytes for float scoreMatch

        // Create a ByteBuffer with the calculated total size
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        // Serialize the fields into the buffer
        buffer.putFloat(faceData.getLive());
        buffer.putInt(features.length);
        buffer.put(features);
        buffer.putInt(bmpBytes.length);
        buffer.put(bmpBytes);
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        buffer.putFloat(faceData.getScoreMatch());

        return buffer.array();
    }
}