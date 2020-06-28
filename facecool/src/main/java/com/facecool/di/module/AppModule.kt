package com.facecool.di.module

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.face.cool.cache.AppCache
import com.face.cool.cache.Cache
import com.face.cool.common.image.ImageManager
import com.face.cool.common.image.PrivateImageManager
import com.face.cool.common.transfer.DataTransferOneTime
import com.face.cool.common.transfer.DataTransferReceiver
import com.face.cool.common.transfer.DataTransferSender
import com.face.cool.databasa.Database
import com.face.cool.databasa.MIGRATION_1_2
import com.face.cool.databasa.MIGRATION_2_3
import com.face.cool.databasa.MIGRATION_3_4
import com.face.cool.databasa.MIGRATION_4_5
import com.face.cool.databasa.MIGRATION_5_6
import com.facecool.FakeFaceDetectionEngine
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.attendance.facedetector.NativeFaceDetectionEngine
import com.facecool.common.AppLogger
import com.facecool.common.FaceCoolAppLogger
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    private const val APP_DATABASE_NAME = "face-app-database"

    @Provides
    @Singleton
    fun provideFaceDetectionEngine(): FaceDetectionEngine = NativeFaceDetectionEngine()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(
            context,
            Database::class.java, APP_DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideImageManager( context: Application): ImageManager =
        PrivateImageManager(context)

    @Provides
    @Singleton
    fun provideCache(
        app: Application
    ): Cache = AppCache(app)

    @Provides
    @Singleton
    fun provideLogger() : AppLogger {
        return FaceCoolAppLogger()
    }

    @Provides
    @Singleton
    fun provideDataTransfer(): DataTransferOneTime = DataTransferOneTime()

    @Provides
    @Singleton
    fun provideDataTransferSender(sender: DataTransferOneTime): DataTransferSender = sender

    @Provides
    @Singleton
    fun provideDataTransferReceiver(receiver: DataTransferOneTime): DataTransferReceiver = receiver

}
