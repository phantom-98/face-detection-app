package com.facecool.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.facecool.R


private lateinit var img: Bitmap
private var flag = false

fun unknownUserImage(context: Context): Bitmap {
    if (!flag){
        img = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.unknown_user
        )
        flag = true
    }
    return img
}
