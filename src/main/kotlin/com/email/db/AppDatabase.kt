package com.email.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
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
import com.email.db.typeConverters.*

/**
 * Created by sebas on 1/24/18.
 */

@Database(entities = [ Email::class, Label::class, EmailLabel::class, Account::class, EmailContact::class
                     , File::class, Open::class, FeedItem::class, CRPreKey::class, Contact::class
                     , CRSessionRecord::class, CRIdentityKey::class, CRSignedPreKey::class],
        version = 1,
        exportSchema = false)
@TypeConverters(
        DateConverter::class,
        BooleanConverter::class,
        LabelColorConverter::class,
        LabelTextConverter::class,
        ContactTypeConverter::class,
        EmailDeliveryConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailDao(): EmailDao
    abstract fun labelDao(): LabelDao
    abstract fun emailLabelDao(): EmailLabelDao
    abstract fun contactDao(): ContactDao
    abstract fun accountDao(): AccountDao
    abstract fun fileDao(): FileDao
    abstract fun openDao(): OpenDao
    abstract fun emailContactDao() : EmailContactJoinDao
    abstract fun feedDao(): FeedDao
    abstract fun rawSessionDao(): RawSessionDao
    abstract fun rawPreKeyDao(): RawPreKeyDao
    abstract fun rawIdentityKeyDao(): RawIdentityKeyDao
    abstract fun rawSignedPreKeyDao(): RawSignedPreKeyDao
    abstract fun resetDao(): ResetDao
    abstract fun signUpDao(): SignUpDao
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
