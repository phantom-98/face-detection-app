package com.facecoolalert.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class FileUtils {

    public static String getFriendlyPathFromUri(Context context, Uri uri) {
        String path = null;
        Log.d("FileUtils", "URI: " + uri+" Path : "+uri.getPath()+" raw "+uri.toString()+" ");

        // Check for "file" scheme (local file)
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // Handle document URI (e.g., Google Drive, Downloads)
            String documentId = DocumentsContract.getDocumentId(uri);
            String authority = uri.getAuthority();

            if ("com.android.providers.downloads.documents".equals(authority)) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                path = getDataColumn(context, contentUri, null, null);
            } else if ("com.android.providers.media.documents".equals(authority)) {
                String[] split = documentId.split(":");
                String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                path = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else {
            // Handle other URIs (e.g., "content" scheme)
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    path = cursor.getString(column_index);
                }
                cursor.close();
            }
        }

        // Extract the friendly path from the absolute path
        if (path != null) {
            String[] segments = path.split("/");
            if (segments.length > 1) {
                return segments[segments.length - 2] + "/" + segments[segments.length - 1];
            }
        }

        if(path==null)
            path=getFileNameFromUri(context,uri);

        return path;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e("FileUtils", "Error getting data column for uri: " + uri, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public static String getFileNameFromUri(Context context, Uri uri) {
        String fileName = uri.getPath().toString();
        Cursor cursor = null;

        try {
            String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
            cursor = context.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                fileName = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            // Handle exceptions, log, or perform error handling as needed
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return fileName;
    }


    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }


}
