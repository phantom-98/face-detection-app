package com.facecool.utils

import android.view.LayoutInflater
import android.view.ViewGroup

fun ViewGroup.inflater(): LayoutInflater = LayoutInflater.from(context)