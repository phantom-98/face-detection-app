package com.facecoolalert.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;

public class GenUtils {

    public static float roundScore(float score)
    {
        float factor=10;

        if(score<1)
            factor*=100;

        return Math.round(score*factor)/10f;
    }


    public static int getInt(String str)
    {
        int fine=0;
        try{
            fine=Integer.parseInt(str);
        }catch (Exception er)
        {

        }
        return fine;
    }

    public static int getColor(String sts)
    {
        return sts.contains("?")&&sts.contains("_")?Color.RED:Color.GREEN;
    }

    public static Bitmap getQualityImage(Face face,Bitmap bmpFrame)
    {
        Rect boundingBox = face.getBoundingBox();
        Bitmap photo=Bitmap.createBitmap(bmpFrame,boundingBox.left,
                boundingBox.top,
                boundingBox.width(),
                boundingBox.height());
        return  photo;
    }

    public static float getFloat(String text) {
        float fine=0f;
        try{
            fine=Float.parseFloat(text);
        }catch (Exception es)
        {
        }
        return fine;
    }

    public static String beautifulStorage(Double size) {
        String units="GB";
        Double value=size;
        if(size<1)
        {
            units="MB";
            value*=1024.0;
        }

        return String.format("%s %s",Math.round(value*1000.0f)/1000.0f,units);
    }

    public static String roundScore(double value) {
        return String.format("%.1f",value);
    }

    public static String roundWhole(double value) {
        return String.format("%.0f",value);
    }
    public static String roundWhole(float value) {
        return String.format("%.0f",value);
    }
}
