package com.email.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.email.db.TypeConverters.BooleanConverter
import com.email.db.TypeConverters.DateConverter
import com.email.db.TypeConverters.LabelColorConverter
import com.email.db.dao.*
import com.email.db.dao.signal.RawIdentityKeyDao
import com.email.db.dao.signal.RawPreKeyDao
import com.email.db.dao.signal.RawSessionDao
import com.email.db.dao.signal.RawSignedPreKeyDao
import com.email.db.models.*
import com.email.db.models.signal.CRIdentityKey
import com.email.db.models.signal.CRPreKey
import com.email.db.models.signal.CRSessionRecord
import com.email.db.models.signal.CRSignedPreKey

/**
 * Created by gabriel on 3/17/18.
 */
@Database(entities = [ Email::class, Label::class, EmailLabel::class, Account::class, EmailContact::class
                     , File::class, Open::class, FeedItem::class, CRPreKey::class, Contact::class
                     , CRSessionRecord::class, CRIdentityKey::class, CRSignedPreKey::class],
        version = 1,
        exportSchema = false)
@TypeConverters(DateConverter::class, BooleanConverter::class, LabelColorConverter::class)
abstract class TestDatabase : AppDatabase() {
    abstract fun resetDao(): ResetDao

    companion object {
        private var INSTANCE : TestDatabase? = null

        fun getAppDatabase(context: Context): TestDatabase {
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context,
                        TestDatabase::class.java,
                        "testdb")
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}