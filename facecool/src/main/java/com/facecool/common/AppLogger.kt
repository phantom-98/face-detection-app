package com.facecool.common

interface AppLogger {

    fun <T>log(tag: T, obj: Any)

}