package com.face.cool.common.image

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class PrivateImageManager constructor(private val context: Application) : ImageManager {

    companion object{
        private const val DIRECTORY = "FaceCool"
    }

    override fun saveImage(image: Bitmap, name: String) {
        ImageSaver(context).setFileName(name).setDirectoryName(DIRECTORY).save(image)
    }

    override fun getImage(name: String): Bitmap? {
        return ImageSaver(context).setFileName(name).setDirectoryName(DIRECTORY).load()
    }

    override fun getImage(name: String, options: BitmapFactory.Options): Bitmap? {
        return ImageSaver(context).setFileName(name).setDirectoryName(DIRECTORY).load(options)
    }

    override fun deleteImage(name: String) {
        ImageSaver(context).setFileName(name).setDirectoryName(DIRECTORY).deleteFile()
    }
}