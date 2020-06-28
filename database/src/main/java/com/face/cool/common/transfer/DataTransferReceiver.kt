package com.face.cool.common.transfer

interface DataTransferReceiver {

    fun <T> get(key: String?): T?

}
