package com.face.cool.common.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory

interface ImageManager {
    fun saveImage(image: Bitmap, name: String)
    fun getImage(name: String): Bitmap?
    fun getImage(name: String, options: BitmapFactory.Options): Bitmap?
    fun deleteImage(name: String)
}
