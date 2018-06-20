package com.email.androidtest

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.email.db.AppDatabase
import com.email.db.dao.ResetDao
import com.email.db.models.*
import com.email.db.models.signal.CRIdentityKey
import com.email.db.models.signal.CRPreKey
import com.email.db.models.signal.CRSessionRecord
import com.email.db.models.signal.CRSignedPreKey
import com.email.db.typeConverters.*

/**
 * Created by gabriel on 5/24/18.
 */
@Database(entities = [ Email::class, Label::class, EmailLabel::class, Account::class, EmailContact::class
                     , CRFile::class, Open::class, FeedItem::class, CRPreKey::class, Contact::class
                     , CRSessionRecord::class, CRIdentityKey::class, CRSignedPreKey::class],
        version = 1,
        exportSchema = false)
@TypeConverters(
        DateConverter::class,
        BooleanConverter::class,
        LabelTypeConverter::class,
        LabelTextConverter::class,
        ContactTypeConverter::class,
        EmailDeliveryConverter::class)
abstract class TestDatabase: AppDatabase() {
    abstract fun resetDao(): ResetDao

    companion object {
        private var INSTANCE: TestDatabase? = null

        fun getInstance(ctx: Context): TestDatabase {
            if (INSTANCE == null)
                INSTANCE = Room.databaseBuilder(ctx, TestDatabase::class.java, "testDB")
                    .allowMainThreadQueries()
                    .build()
            return INSTANCE!!
        }
    }
}