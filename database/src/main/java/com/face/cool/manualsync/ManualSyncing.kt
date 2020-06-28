package com.face.cool.manualsync

interface ManualSyncing<From, To> {

    suspend fun generateDB(data: From): To

    suspend fun syncDB(data: To): From

    fun generateExpertDataWrapper(vararg paramList: Any?): From

}
