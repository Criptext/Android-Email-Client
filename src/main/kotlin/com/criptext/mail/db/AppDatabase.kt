package com.criptext.mail.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.dao.signal.RawIdentityKeyDao
import com.criptext.mail.db.dao.signal.RawPreKeyDao
import com.criptext.mail.db.dao.signal.RawSessionDao
import com.criptext.mail.db.dao.signal.RawSignedPreKeyDao
import com.criptext.mail.db.models.*
import com.criptext.mail.db.models.signal.CRIdentityKey
import com.criptext.mail.db.models.signal.CRPreKey
import com.criptext.mail.db.models.signal.CRSessionRecord
import com.criptext.mail.db.models.signal.CRSignedPreKey
import com.criptext.mail.db.typeConverters.*
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import java.util.*


/**
 * Created by sebas on 1/24/18.
 */

@Database(entities = [ Email::class, Label::class, EmailLabel::class, Account::class, EmailContact::class
                     , CRFile::class, FileKey::class, Open::class, FeedItem::class, CRPreKey::class, Contact::class
                     , CRSessionRecord::class, CRIdentityKey::class, CRSignedPreKey::class, EmailExternalSession::class
                     , PendingEvent::class],
        version = 4,
        exportSchema = false)
@TypeConverters(
        DateConverter::class,
        BooleanConverter::class,
        LabelTypeConverter::class,
        ContactTypeConverter::class,
        EmailDeliveryConverter::class,
        FeedTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun contactDao(): ContactDao
    abstract fun emailDao(): EmailDao
    abstract fun emailLabelDao(): EmailLabelDao
    abstract fun fileDao(): FileDao
    abstract fun fileKeyDao(): FileKeyDao
    abstract fun emailContactDao() : EmailContactJoinDao
    abstract fun feedDao(): FeedItemDao
    abstract fun labelDao(): LabelDao
    abstract fun emailInsertionDao(): EmailInsertionDao
    abstract fun rawIdentityKeyDao(): RawIdentityKeyDao
    abstract fun rawPreKeyDao(): RawPreKeyDao
    abstract fun rawSessionDao(): RawSessionDao
    abstract fun rawSignedPreKeyDao(): RawSignedPreKeyDao
    abstract fun signUpDao(): SignUpDao
    abstract fun openDao(): OpenDao
    abstract fun emailExternalSessionDao(): EmailExternalSessionDao
    abstract fun pendingEventDao(): PendingEventDao
    companion object {
        private var INSTANCE : AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase {
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context,
                        AppDatabase::class.java,
                        "encriptedMail1")
                        //allowMainThreadQueries() // remove this in production... !!!!
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .build()
            }
            return INSTANCE!!
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""ALTER TABLE email ADD COLUMN trashDate INTEGER""")
                for(label in Label.defaultItems.toList()) {
                    database.execSQL("""UPDATE label SET text = '${label.text}' WHERE text = '${label.text.toUpperCase()}'""")
                }
                database.execSQL("""UPDATE email
                        SET trashDate = CAST('${Date()}' AS DATE)
                        WHERE  email.id = (SELECT email_label.emailId FROM email_label
                                            WHERE email_label.labelId = (SELECT label.id FROM label
                                                                            WHERE label.text = '${Label.LABEL_TRASH}'))""")
                database.execSQL("""CREATE TABLE IF NOT EXISTS  email_external_session (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                        emailId INTEGER NOT NULL,
                                        iv TEXT NOT NULL,
                                        salt TEXT NOT NULL,
                                        encryptedSession TEXT NOT NULL,
                                        encryptedBody TEXT NOT NULL,
                                        FOREIGN KEY(emailId) REFERENCES email(id) ON DELETE CASCADE)""")
                database.execSQL("CREATE INDEX index_email_external_session_emailId ON email_external_session (emailId)")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""UPDATE OR IGNORE contact
                                        SET email = replace(email, rtrim(email, replace(email, ' ', '')), '')
                                        """)
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS  pendingEvent (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                        data TEXT NOT NULL)""")
                database.execSQL("CREATE INDEX index_pending_event_id ON pendingEvent (id)")
            }
        }
    }
}
