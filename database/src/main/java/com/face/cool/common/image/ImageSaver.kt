package com.face.cool.common.image

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class ImageSaver(private val context: Application) {
    private var directoryName = "images"
    private var fileName = "image.png"
//    private val context: Context

//    init {
//        this.context = context
//    }

    fun setFileName(fileName: String): ImageSaver {
        this.fileName = fileName
        return this
    }

    fun setDirectoryName(directoryName: String): ImageSaver {
        this.directoryName = directoryName
        return this
    }

    fun save(bitmapImage: Bitmap) {
        FileOutputStream(createFile()).use {
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun createFile(): File {
        val directory: File = context.getDir(directoryName, Context.MODE_PRIVATE)
        if (!directory.exists() && !directory.mkdirs()) {
//            Log.e("ImageSaver", "Error creating directory $directory")
        }
        return File(directory, fileName)
    }

    fun load(options: BitmapFactory.Options? = null): Bitmap? {
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(createFile())
            if (options == null) {
                return BitmapFactory.decodeStream(inputStream)
            }
            return BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun deleteFile(): Boolean {

//        Log.d("CAMCHANGETAG", "deleteFile")

        val file: File = createFile()

//        Log.d("CAMCHANGETAG", "file $file")

        val d = file.delete()

//        Log.d("CAMCHANGETAG", "file s=delete $d")


        if (file.exists()) {
//            Log.d("CAMCHANGETAG", "file delrte 1")
            file.canonicalFile.delete()
            if (file.exists()) {
//                Log.d("CAMCHANGETAG", "file delrte 2")
                context.deleteFile(file.name)
            }
        }
        return true


//        val directory: File = context.getDir(directoryName, Context.MODE_PRIVATE)
//        return directory.delete()
    }

}
