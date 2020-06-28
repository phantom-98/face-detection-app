package com.face.cool.common.transfer

class DataTransferOneTime : DataTransferSender, DataTransferReceiver {

    private val dataMap = mutableMapOf<String, Any>()

    override fun <T> get(key: String?): T? {
        if (key == null) return null
        val rez = dataMap[key] as? T?
        dataMap.remove(key)
        return rez
    }

    override fun <T> put(key: String, value: T) {
        dataMap[key] = value as Any
    }
}
