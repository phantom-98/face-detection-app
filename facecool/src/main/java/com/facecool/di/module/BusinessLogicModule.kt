package com.facecool.di.module

import com.face.cool.cache.Cache
import com.face.cool.common.image.ImageManager
import com.face.cool.databasa.Database
import com.face.cool.manualsync.ExpertDataWrapper
import com.face.cool.manualsync.JsonManualSyncing
import com.face.cool.manualsync.ManualSyncing
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.ui.camera.businesslogic.events.EventMapper
import com.facecool.ui.camera.businesslogic.events.EventMapperImpl
import com.facecool.ui.camera.businesslogic.events.EventRepository
import com.facecool.ui.camera.businesslogic.events.EventRepositoryImpl
import com.facecool.ui.camera.businesslogic.user.DatabaseUserRepository
import com.facecool.ui.camera.businesslogic.user.UserMapper
import com.facecool.ui.camera.businesslogic.user.UserMapperImpl
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.classes.common.ClassMapper
import com.facecool.ui.classes.common.ClassMapperImpl
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.classes.common.ClassRepositoryImpl
import com.facecool.ui.common.lessons.LessonMapper
import com.facecool.ui.common.lessons.LessonMapperImpl
import com.facecool.ui.common.lessons.LessonRepository
import com.facecool.ui.common.lessons.LessonRepositoryImpl
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
object BusinessLogicModule {

    @Provides
    fun provideDataSync(
        imageManager: ImageManager,
        gson: Gson
    ): ManualSyncing<ExpertDataWrapper, String> {
        return JsonManualSyncing(imageManager, gson)
    }

    @Provides
    fun provideCameraRepository(
        database: Database,
        mapper: UserMapper,
        imageManager: ImageManager,
        faceDetectionEngine: FaceDetectionEngine,
        cache: Cache,
    ): UserRepository {
        return DatabaseUserRepository(
            database, mapper, imageManager, faceDetectionEngine, cache
        )
    }

    @Provides
    fun provideUserMapper(
        gson: Gson,
        imageManager: ImageManager,
        eventMapper: EventMapper
    ): UserMapper {
        return UserMapperImpl(gson, imageManager, eventMapper)
    }

    @Provides
    fun provideEventMapper(imageManager: ImageManager): EventMapper = EventMapperImpl(imageManager)


    @Provides
    fun providesEventRepository(
        database: Database,
        eventMapper: EventMapper
    ): EventRepository {
        return EventRepositoryImpl(database, eventMapper)
    }

    @Provides
    fun provideClassMapper(): ClassMapper = ClassMapperImpl()

    @Provides
    fun provideClassRepository(
        database: Database,
        classMapper: ClassMapper
    ): ClassRepository {
        return ClassRepositoryImpl(
            database,
            classMapper,
        )
    }

    @Provides
    fun provideLessonMapper(): LessonMapper {
        return LessonMapperImpl()
    }

    @Provides
    fun provideLessonRepository(
        database: Database,
        lessonMapper: LessonMapper
    ): LessonRepository {
        return LessonRepositoryImpl(database, lessonMapper)
    }


}
