package com.email.DB

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.email.DB.DAO.*
import com.email.DB.TypeConverters.BooleanConverter
import com.email.DB.TypeConverters.DateConverter
import com.email.DB.models.*

/**
 * Created by sebas on 1/24/18.
 */

@Database(entities = arrayOf(Email::class,
        Label::class,
        EmailLabel::class,
        User::class,
        EmailUser::class,
        File::class,
        Open::class,
        Feed::class
        ),
        version = 1,
        exportSchema = false)
@TypeConverters(DateConverter::class, BooleanConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailDao(): EmailDao
    abstract fun labelDao(): LabelDao
    abstract fun emailLabelDao(): EmailLabelJoinDao
    abstract fun userDao(): UserDao
    abstract fun fileDao(): FileDao
    abstract fun openDao(): OpenDao
    abstract fun emailUserDao() : EmailUserJoinDao
    abstract fun feedDao(): FeedDao
    companion object {
        private var INSTANCE : AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase {
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context,
                        AppDatabase::class.java,
                        "encriptedMail")
                        .allowMainThreadQueries() // remove this in production... !!!!
                        // .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                        .build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
