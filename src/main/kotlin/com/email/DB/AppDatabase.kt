package com.email.DB

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.email.DB.DAO.EmailDao
import com.email.DB.DAO.EmailLabelJoinDao
import com.email.DB.DAO.LabelDao
import com.email.DB.TypeConverters.DateConverter
import com.email.DB.models.Email
import com.email.DB.models.EmailLabel
import com.email.DB.models.Label

/**
 * Created by sebas on 1/24/18.
 */

@Database(entities = arrayOf(Email::class, Label::class, EmailLabel::class), version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailDao(): EmailDao
    abstract fun labelDao(): LabelDao
    abstract fun emailLabelDao() : EmailLabelJoinDao

    companion object {
        private var INSTANCE : AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase? {
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, "encripted_mail")
                        .allowMainThreadQueries() // remove this in production... !!!!
                        // .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                        .build()
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
