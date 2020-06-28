package com.facecool.utils

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.facecool.BuildConfig
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException


fun getExternalFilesDirCompat(folderName: String, context: Context): File? {
    var file: File? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        file = context.getExternalFilesDir(folderName)
    } else {
        val externalDir = Environment.getExternalStorageDirectory()
        if (externalDir != null) {
            file =
                File(externalDir.absolutePath + "/Android/data/" + context.packageName + "/files/" + folderName)
            if (!file.exists()) {
                file.mkdirs()
            }
        }
    }
    return file
}


fun Context.shareReport(cvsData: String) {
    try {
        val filename = "student_report_for_" + System.currentTimeMillis()
            .getReadableDate(pattern = "MMMM_dd_yyyy") + ".csv"
        val file =
            File(getExternalFilesDirCompat("reports", this), filename)
        val fw = FileWriter(file.absoluteFile)
        val bw = BufferedWriter(fw)
        bw.write(cvsData)
        bw.close()
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun Context.shareData(data: String) {
    try {
        val filename = "face_cool_db_" + System.currentTimeMillis().getReadableDate(pattern = "MMMM_dd_yyyy_HH_mm") + ".fcud"
        val file =
            File(getExternalFilesDirCompat("student_data", this), filename)
        val fw = FileWriter(file.absoluteFile)
        val bw = BufferedWriter(fw)
        bw.write(data)
        bw.close()
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun loadData(file: File): String {

    //Read text from file
    val text = StringBuilder()

    try {
        val br = BufferedReader(FileReader(file))
        var line: String?
        while (br.readLine().also { line = it } != null) {
            text.append(line)
            text.append('\n')
        }
        br.close()
    } catch (e: IOException) {
        //You'll need to add proper error handling here
    }

    return text.toString()

}


//fun getPath(context: Context?, uri: Uri): String? {
//
//    // check here to KITKAT or new version
//    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
//
//    // DocumentProvider
//    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
//
//        // ExternalStorageProvider
//        if (isExternalStorageDocument(uri)) {
//            val docId = DocumentsContract.getDocumentId(uri)
//            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
//                .toTypedArray()
//            val type = split[0]
//            if ("primary".equals(type, ignoreCase = true)) {
//                return (Environment.getExternalStorageDirectory().toString() + "/"
//                        + split[1])
//            }
//        } else if (isDownloadsDocument(uri)) {
//            val id = DocumentsContract.getDocumentId(uri)
//            val contentUri = ContentUris.withAppendedId(
//                Uri.parse("content://downloads/public_downloads"),
//                java.lang.Long.valueOf(id)
//            )
//            return getDataColumn(context, contentUri, null, null)
//        } else if (isMediaDocument(uri)) {
//            val docId = DocumentsContract.getDocumentId(uri)
//            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
//                .toTypedArray()
//            val type = split[0]
//            var contentUri: Uri? = null
//            if ("image" == type) {
//                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            } else if ("video" == type) {
//                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//            } else if ("audio" == type) {
//                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//            }
//            val selection = "_id=?"
//            val selectionArgs = arrayOf(split[1])
//            return getDataColumn(
//                context, contentUri, selection,
//                selectionArgs
//            )
//        }
//    } else if ("content".equals(uri.scheme, ignoreCase = true)) {
//
//        // Return the remote address
//        return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
//            context,
//            uri,
//            null,
//            null
//        )
//    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
//        return uri.path
//    }
//    return null
//}


object FilePath {
    /**
     * Method for return file path of Gallery image/ Document / Video / Audio
     *
     * @param context
     * @param uri
     * @return path of the selected image file from gallery
     */
    fun getPath(context: Context, uri: Uri): String? {

        // check here to KITKAT or new version
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return (Environment.getExternalStorageDirectory().toString() + "/"
                            + split[1])
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
//                val contentUri = ContentUris.withAppendedId(
////                    Uri.parse("content://downloads/public_downloads"),
//                    Uri.parse("content://com.android.providers.downloads.documents/"),
//                    java.lang.Long.valueOf(id)
//                )
//                return getDataColumn(context, contentUri, null, null)
                return getDataColumn(context, uri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return contentUri?.let { getDataColumn(context, it, selection, selectionArgs) }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     * The context.
     * @param uri
     * The Uri to query.
     * @param selection
     * (Optional) Filter used in the query.
     * @param selectionArgs
     * (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri
            .authority
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri
            .authority
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri
            .authority
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri
            .authority
    }
}

