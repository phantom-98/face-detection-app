package com.facecool.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.facecool.R
import java.io.Serializable
import java.util.Calendar


inline fun <reified T : Serializable> Activity.getExtraClass(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getSerializableExtra(key, T::class.java)
    } else {
        intent.getSerializableExtra(key) as T?
    }
}

fun Context.openDateSelector(min: Long = 0, max: Long = 0, default: Long = 0, onDateSelected: (time: Long) -> Unit) {
    val c: Calendar = Calendar.getInstance()
    if (default>0) c.timeInMillis = default
    val mYear = c.get(Calendar.YEAR)
    val mMonth = c.get(Calendar.MONTH)
    val mDay = c.get(Calendar.DAY_OF_MONTH)
    val datePickerDialog = DatePickerDialog(
        this,
        { _, year, monthOfYear, dayOfMonth ->
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            onDateSelected(c.timeInMillis)
        },
        mYear,
        mMonth,
        mDay
    )
    if (min>0) datePickerDialog.datePicker.minDate = min
    if (max>0) datePickerDialog.datePicker.maxDate = max
    datePickerDialog.show()
}

fun Context.openTimeSelector( default: Long = 0, onTimeSelected: (time: Long) -> Unit) {
    val c = Calendar.getInstance()
    if (default>0) c.timeInMillis = default
    val mHour = c[Calendar.HOUR_OF_DAY]
    val mMinute = c[Calendar.MINUTE]
    val timePickerDialog = TimePickerDialog(
        this,
        { _, hourOfDay, minute ->
            c.set(Calendar.YEAR, 1970)
            c.set(Calendar.MONTH, 0)
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.set(Calendar.HOUR_OF_DAY, hourOfDay)
            c.set(Calendar.MINUTE, minute)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
//            if (min > 0) {
//                val readableTime = c.timeInMillis.getReadableDate(pattern = "HH:mm")
//                val minTime = min.getReadableDate(pattern = "HH:mm")
//                if (readableTime < minTime){
////                    Toast.makeText(this, getString(R.string.validation_time_min, minTime), Toast.LENGTH_SHORT).show()
////                    return@TimePickerDialog
//                    c.set(Calendar.DAY_OF_MONTH, 2)
//                }
//            }
//            if (max > 0) {
//                val readableTime = c.timeInMillis.getReadableDate(pattern = "HH:mm")
//                val maxTime = max.getReadableDate(pattern = "HH:mm")
//                if (readableTime > maxTime){
//                    Toast.makeText(this, getString(R.string.validation_time_max, maxTime), Toast.LENGTH_SHORT).show()
//                    return@TimePickerDialog
//                }
//            }
            onTimeSelected(c.timeInMillis)

        },
        mHour,
        mMinute,
        true
    )
    timePickerDialog.show()
}
