package com.facecool.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException

fun ImageView.loadImagesWithGlideExt(url: String) {
    Glide.with(this)
        .load(url)
//        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.loadUriImage(uri: Uri) {
    Glide.with(this)
        .load(uri)
        .override(200)
//        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun Context.loadUriIe(uri: Uri): Bitmap? {
    val ctx = this
    return suspendCancellableCoroutine {
        Glide.with(ctx)
            .asBitmap()
            .load(uri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    it.resume(resource) { thr ->
                        thr.printStackTrace()
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
    }
}


fun Activity.uriToBitmap(selectedFileUri: Uri): Bitmap? {
    try {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}


private const val DIRECTORY = "FaceCool"

fun ImageView.loadImageFromLocal(imageName: String) {
    val directory: File = context.getDir(DIRECTORY, Context.MODE_PRIVATE)
    val f = File(directory, imageName)
    Glide.with(this)
        .load(f)
//        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(this)
}

//fun ImageView.loadImageFromLocalNoCache(imageName: String) {
//    val directory: File = context.getDir(DIRECTORY, Context.MODE_PRIVATE)
//    val f = File(directory, imageName)
//    Glide.with(this)
//        .load(f)
////        .centerCrop()
////        .diskCacheStrategy(DiskCacheStrategy.ALL)
//        .into(this)
//}


fun ImageView.setIcon(icon: Int) {
    Glide.with(this)
        .load(icon)
//        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}
