package com.email.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.email.db.DAO.*
import com.email.db.TypeConverters.BooleanConverter
import com.email.db.TypeConverters.DateConverter
import com.email.db.dao.RawSessionDao
import com.email.db.models.*
import com.email.db.models.signal.RawSession

/**
 * Created by sebas on 1/24/18.
 */

@Database(entities = [ Email::class, Label::class, EmailLabel::class, User::class, EmailUser::class
                     , File::class, Open::class, FeedItem::class, RawSession::class],
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
    abstract fun rawSessionDao(): RawSessionDao
    companion object {
        private var INSTANCE : AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase {
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context,
                        AppDatabase::class.java,
                        "encriptedMail1")
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
