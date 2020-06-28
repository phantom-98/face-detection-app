package com.face.cool.manualsync

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.face.cool.common.image.ImageManager
import com.face.cool.databasa.classes.ClassEntity
import com.face.cool.databasa.detection_events.EventEntity
import com.face.cool.databasa.lessons.LessonEntity
import com.face.cool.databasa.users.UserEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class JsonManualSyncing constructor(
    private val imageManager: ImageManager,
    private val gson: Gson
) : ManualSyncing<ExpertDataWrapper, String> {

    companion object {
        private const val SCALED_SIE = 20
    }

    private val options = BitmapFactory.Options()

    override suspend fun syncDB(data: String): ExpertDataWrapper {
        val listType: Type = object : TypeToken<ExpertDataWrapper>() {}.type
        val wrapperData = gson.fromJson<ExpertDataWrapper>(data, listType)
        wrapperData.userList.onEach {
            val btm = it?.image?.stringToBitMap()
            btm?.let { it1 -> imageManager.saveImage(it1, it.user?.imageLocalAddress ?: "") }
            it?.user?.uid = null
            it?.bitmap = btm
        }
        wrapperData.eventList.onEach {
            val btm = it?.image?.stringToBitMap()
            btm?.let { it1 -> imageManager.saveImage(it1, it.event?.imageName ?: "") }
            it?.bitmap = btm
            it?.event?.uid = null
        }
        wrapperData?.lessonList?.onEach {
            it?.uid = null
        }
        wrapperData?.classList?.onEach {
            it?.uid = null
        }
        return wrapperData
    }

    override fun generateExpertDataWrapper(vararg paramList: Any?): ExpertDataWrapper {
        val data = mutableListOf<UserEntity>()
        val eventList = mutableListOf<EventEntity>()
        val classList = mutableListOf<ClassEntity>()
        val lessonList = mutableListOf<LessonEntity>()

        fun checkForItemType(item: Any?) {
            if (item is UserEntity) {
                item.uid = null
                data.add(item)
                return
            }
            if (item is ClassEntity) {
                item.uid = null
                classList.add(item)
                return
            }
            if (item is LessonEntity) {
                item.uid = null
                lessonList.add(item)
                return
            }
            if (item is EventEntity) {
                item.uid = null
                eventList.add(item)
                return
            }
        }

        fun checkForList(lst: Any?) {
            if (lst is Collection<*>) {
                lst.forEach {
                    checkForItemType(it)
                }
            }
        }

        paramList.forEach {
            checkForItemType(it)
            checkForList(it)
        }
        val dataToSave = mutableListOf<UserDataSync>()
        var btm: Bitmap? = null

        options.inSampleSize = 4

        data.forEach {
            it.imageLocalAddress?.let { it1 ->
                btm = imageManager.getImage(it1, options)
                val etsBtm = btm?.bitMapToString()
                dataToSave.add(
                    UserDataSync(it, etsBtm)
                )
            }
        }

        val eventDataSync = mutableListOf<EventDataSync>()
        eventList.forEach {
            it.imageName.let { it1 ->
                btm = imageManager.getImage(it1, options)
                var etsBtm: String? = null
                if (it.userName.lowercase() == "unidentified" && btm != null)
                    etsBtm= Bitmap.createScaledBitmap(btm!!, 40, 40, false).bitMapToString()
                eventDataSync.add(
                    EventDataSync(it, etsBtm)
                )
            }
        }

        btm?.recycle()
        return ExpertDataWrapper(dataToSave, classList, lessonList, eventDataSync)
    }

    override suspend fun generateDB(data: ExpertDataWrapper): String {
        val listType: Type = object : TypeToken<ExpertDataWrapper>() {}.type
        return gson.toJson(data, listType)
    }

}
