package com.face.cool.databasa

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.face.cool.common.FaceCoolTypeConverters
import com.face.cool.databasa.administrators.AdministratorDao
import com.face.cool.databasa.administrators.AdministratorEntity
import com.face.cool.databasa.classes.ClassDao
import com.face.cool.databasa.classes.ClassEntity
import com.face.cool.databasa.detection_events.EventDao
import com.face.cool.databasa.detection_events.EventEntity
import com.face.cool.databasa.lessons.LessonDao
import com.face.cool.databasa.lessons.LessonEntity
import com.face.cool.databasa.users.UserDao
import com.face.cool.databasa.users.UserEntity


@androidx.room.Database(
    entities = [
        UserEntity::class,
        EventEntity::class,
        ClassEntity::class,
        LessonEntity::class,
        AdministratorEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(value = [FaceCoolTypeConverters::class])
abstract class Database : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun eventDao(): EventDao

    abstract fun classDao(): ClassDao

    abstract fun lessonDao(): LessonDao

    abstract fun adminDao(): AdministratorDao

}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `errors` VARCHAR(255) DEFAULT NULL;")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `twin` INTEGER DEFAULT 0;")
        database.execSQL("CREATE TABLE `admin_table`(" +
                "`uid` INTEGER," +
                "`name` TEXT," +
                "`base64Image` TEXT," +
                "`detectedFaceJson` TEXT," +
                "`realLiveProbability` REAL," +
                "`detectedFeatures` BLOB," +
                "`email` TEXT," +
                "`lastName` TEXT," +
                "`creationTime` INTEGER," +
                "`phoneNumber` TEXT," +
                "`pin` TEXT," +
                "PRIMARY KEY(`uid`)" +
                ");")
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `lesson_table` ADD COLUMN `autoKiosk` INTEGER DEFAULT 0;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `deleted` INTEGER DEFAULT 0;")
    }
}

val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `event_table` ADD COLUMN `lessonId` INTEGER;")
        database.execSQL("ALTER TABLE `event_table` ADD COLUMN `prevStatus` TEXT;")
    }
}

val MIGRATION_5_6: Migration = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `lesson_table` ADD COLUMN `liveness` INTEGER DEFAULT 0;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `phoneNumber` TEXT;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `enableNotification` INTEGER DEFAULT 1;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `parentNames` TEXT;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `enableSMS` INTEGER DEFAULT 1;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `enableEmail` INTEGER DEFAULT 1;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `enableWhatsapp` INTEGER DEFAULT 1;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `parentSMSNumbers` TEXT;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `parentEmails` TEXT;")
        database.execSQL("ALTER TABLE `user_table` ADD COLUMN `parentWhatsappNumbers` TEXT;")
    }
}