package com.face.cool.common.transfer

interface DataTransferSender {

    fun <T> put(key: String, value: T)

}
