package com.facecool.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> {
    return this
}

fun <T> liveEvent(): MutableLiveData<LiveEvent<T>> {
    return MutableLiveData<LiveEvent<T>>()
}

fun <T> MutableLiveData<LiveEvent<T>>.asLiveData(): LiveData<LiveEvent<T>> {
    return this
}

fun <T>MutableLiveData<LiveEvent<T>>.update(v: T?){
    this.postValue(LiveEvent(v))
}


fun <T> debounce(
    waitMs: Long = 300L,
    coroutineScope: CoroutineScope,
    destinationFunction: (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }
}

fun Long.getReadableDate(
    locale: Locale = Locale.getDefault(),
    pattern: String = "MMM dd, yyyy HH:mm:ss"
): String {
    val formatter = SimpleDateFormat(pattern, locale)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return formatter.format(calendar.time)
}

fun Long.getDate() : Long{
    val date = this.getReadableDate(pattern = "MMM dd, yyyy")
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(date).time
}

class LiveEvent<T>(
    private val t: T?
) {
    private var isTaken = false
    fun getValue() = t
    fun getValueIfNotUsed(): T? {
        if (isTaken) return null
        else {
            isTaken = true
            return t
        }
    }
}
