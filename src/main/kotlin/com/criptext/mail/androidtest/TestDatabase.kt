package com.criptext.mail.androidtest

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.dao.ResetDao
import com.criptext.mail.db.models.*
import com.criptext.mail.db.models.signal.CRIdentityKey
import com.criptext.mail.db.models.signal.CRPreKey
import com.criptext.mail.db.models.signal.CRSessionRecord
import com.criptext.mail.db.models.signal.CRSignedPreKey
import com.criptext.mail.db.typeConverters.*

/**
 * Created by gabriel on 5/24/18.
 */
@Database(entities = [ Email::class, Label::class, EmailLabel::class, Account::class, EmailContact::class
                     , CRFile::class, FileKey::class, Open::class, FeedItem::class, CRPreKey::class, Contact::class
                     , CRSessionRecord::class, CRIdentityKey::class, CRSignedPreKey::class, EmailExternalSession::class],
        version = 1,
        exportSchema = false)
@TypeConverters(
        DateConverter::class,
        BooleanConverter::class,
        LabelTypeConverter::class,
        ContactTypeConverter::class,
        EmailDeliveryConverter::class,
        FeedTypeConverter::class)
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