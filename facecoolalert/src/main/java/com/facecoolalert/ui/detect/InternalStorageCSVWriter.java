package com.facecoolalert.ui.detect;

import static com.facecoolalert.ui.detect.InternalStorageFacePhotoWriter.saveImageToInternalStorage;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.facecool.attendance.facedetector.FaceData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InternalStorageCSVWriter {

    private static InternalStorageCSVWriter instance;
    private FileWriter fileWriter;
    private String[] columnLabels = {"image", "width/height", "brightness", "sharpness", "yaw", "pitch", "roll", "score", "img_quality"};
    private File face;
    private Context context;

    private InternalStorageCSVWriter(Context context) {
        this.context = context;
    }

    public static InternalStorageCSVWriter getInstance(Context context) {
        if (instance == null) {
            instance = new InternalStorageCSVWriter(context);
        }
        return instance;
    }

    public void initializeCSV(String fileName) {
        File externalDir = context.getExternalFilesDir(null);
        face = new File(externalDir, fileName);
        try {
            fileWriter = new FileWriter(face, true); // true for append mode
            fileWriter.append(TextUtils.join(",", columnLabels));
            fileWriter.append("\n");
            fileWriter.flush();
        } catch (IOException e) {
            Log.e("Initialize CSV", e.getMessage());
        }
    }

    public void appendRowToCSV(String[] rowData) {
        if (instance.fileWriter == null || instance.face == null) {
            Log.e("Append Row", "CSV not initialized");
            return;
        }

        try {
            if (rowData.length != instance.columnLabels.length) {
                Log.e("Append Row", "Row size does not match column label size");
                return;
            }
            instance.fileWriter.append(TextUtils.join(",", rowData));
            instance.fileWriter.append("\n");
            instance.fileWriter.flush();
        } catch (IOException e) {
            Log.e("Append Row", e.getMessage());
        }
    }

    public void closeCSV() {
        if (instance.fileWriter != null) {
            try {
                instance.fileWriter.close();
                instance.fileWriter = null; // Reset fileWriter after closing
            } catch (IOException e) {
                Log.e("Close CSV", e.getMessage());
            }
        }
    }
}
