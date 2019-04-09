package com.criptext.mail.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import android.database.DatabaseUtils
import android.util.Log
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
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.criptext.mail.utils.sha256
import com.github.kittinunf.result.Result
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import java.util.*


/**
 * Created by sebas on 1/24/18.
 */

@Database(entities = [ Email::class, Label::class, EmailLabel::class, Account::class, EmailContact::class
                     , CRFile::class, FileKey::class, Open::class, FeedItem::class, CRPreKey::class, Contact::class
                     , CRSessionRecord::class, CRIdentityKey::class, CRSignedPreKey::class, EmailExternalSession::class
                     , PendingEvent::class, AccountContact::class],
        version = 11,
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
    abstract fun accountContactDao(): AccountContactDao
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
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                                MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                                MIGRATION_9_10, MIGRATION_10_11)
                        .openHelperFactory(RequerySQLiteOpenHelperFactory())
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

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""ALTER TABLE file ADD COLUMN shouldDuplicate INTEGER NOT NULL DEFAULT 0""")
            }
        }

        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""ALTER TABLE account ADD COLUMN refreshToken TEXT NOT NULL DEFAULT ''""")
            }
        }

        val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""ALTER TABLE file ADD COLUMN fileKey TEXT NOT NULL DEFAULT ''""")
            }
        }

        val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""ALTER TABLE email ADD COLUMN fromAddress TEXT NOT NULL DEFAULT ''""")
                database.execSQL("""ALTER TABLE email ADD COLUMN replyTo TEXT DEFAULT NULL""")
                database.execSQL("""ALTER TABLE contact ADD COLUMN isTrusted INTEGER NOT NULL DEFAULT 0""")
                database.execSQL("""ALTER TABLE label ADD COLUMN uuid TEXT NOT NULL DEFAULT ''""")
                for(label in Label.defaultItems.toList()) {
                    val labelId = label.id.toInt()
                    database.execSQL("""UPDATE label SET uuid = '${label.uuid}' WHERE id = $labelId""")
                }
                val cursorCustomLabels = database.query("""SELECT * FROM label WHERE type = 'CUSTOM'""")
                while (cursorCustomLabels.moveToNext()){
                    val labelText = cursorCustomLabels.getString(cursorCustomLabels.getColumnIndex("text"))
                    val shaChars = labelText
                            .sha256("HEX").subSequence(0,4).toString()
                    database.execSQL("""UPDATE label SET uuid = '00000000-0000-0000-0000-00000000$shaChars' WHERE id =
                        ${cursorCustomLabels.getString(cursorCustomLabels.getColumnIndex("id"))}""")
                }
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_label_uuid ON label (uuid)")
                val cursor = database.query("""SELECT contact.email, contact.name, email.id FROM contact, email
                    INNER JOIN email_contact
                    ON contact.id=email_contact.contactId
                    WHERE email_contact.emailId = email.id
                    AND email_contact.type = 2""")
                while(cursor.moveToNext()){
                    val fromEmail = (if(cursor.getString(cursor.getColumnIndex("name")).isEmpty())
                        cursor.getString(cursor.getColumnIndex("email"))
                    else
                        cursor.getString(cursor.getColumnIndex("name")).plus(" <" + cursor.getString(cursor.getColumnIndex("email")) + ">"))
                            .replace("\"", "")
                    database.execSQL("""UPDATE email SET fromAddress = "$fromEmail"
                    WHERE email.id = ${cursor.getString(cursor.getColumnIndex("id"))}
                """)
                }
            }
        }

        val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""ALTER TABLE email ADD COLUMN boundary TEXT DEFAULT NULL""")
                database.execSQL("""ALTER TABLE file ADD COLUMN cid TEXT DEFAULT NULL""")
            }
        }

        val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""ALTER TABLE contact ADD COLUMN score INTEGER NOT NULL DEFAULT 0""")
            }
        }

        val MIGRATION_10_11: Migration = object: Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.beginTransaction()
                try {
                    database.execSQL("""ALTER TABLE account ADD COLUMN id INTEGER DEFAULT 1""")
                    database.execSQL("""ALTER TABLE account ADD COLUMN isActive INTEGER DEFAULT 1""")
                    database.execSQL("""ALTER TABLE account ADD COLUMN isLoggedIn INTEGER DEFAULT 1""")
                    database.execSQL("""ALTER TABLE account ADD COLUMN domain TEXT DEFAULT 'criptext.com'""")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS  new_account (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            recipientId TEXT NOT NULL,
                                            deviceId INTEGER NOT NULL,
                                            name TEXT NOT NULL,
                                            jwt TEXT NOT NULL,
                                            refreshToken TEXT NOT NULL,
                                            identityKeyPairB64 TEXT NOT NULL,
                                            registrationId INTEGER NOT NULL,
                                            signature TEXT NOT NULL,
                                            domain TEXT NOT NULL,
                                            isActive INTEGER NOT NULL,
                                            isLoggedIn INTEGER NOT NULL)""")
                    database.execSQL(
                            """INSERT INTO new_account (id, recipientId, deviceId, name, jwt, refreshToken, identityKeyPairB64,
                                registrationId, signature, domain, isActive, isLoggedIn)
                                SELECT id, recipientId, deviceId, name, jwt, refreshToken, identityKeyPairB64,
                                registrationId, signature, domain, isActive, isLoggedIn
                                FROM account""")

                    database.execSQL("DROP TABLE account")
                    database.execSQL("ALTER TABLE new_account RENAME TO account")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS account_recipient_index ON account (recipientId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_account_name ON account (name)")

                    val account = database.query("SELECT * FROM account WHERE isActive=1")
                    account.moveToNext()
                    
                    val operation = Result.of { account.getLong(account.getColumnIndex("id")) }
                    
                    val accountId = when(operation){
                        is Result.Success -> operation.value
                        is Result.Failure -> 1
                    }

                    database.execSQL("""ALTER TABLE email ADD COLUMN accountId INTEGER DEFAULT $accountId""")

                    database.execSQL("""CREATE TABLE IF NOT EXISTS  new_email (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            messageId TEXT NOT NULL,
                                            threadId TEXT NOT NULL,
                                            fromAddress TEXT NOT NULL,
                                            replyTo TEXT,
                                            boundary TEXT,
                                            unread INTEGER NOT NULL,
                                            secure INTEGER NOT NULL,
                                            content TEXT NOT NULL,
                                            bodyPreview TEXT NOT NULL,
                                            subject TEXT NOT NULL,
                                            delivered INTEGER NOT NULL,
                                            unsentDate INTEGER,
                                            date INTEGER NOT NULL,
                                            metadataKey INTEGER NOT NULL,
                                            isMuted INTEGER NOT NULL,
                                            trashDate INTEGER,
                                            accountId INTEGER NOT NULL,
                                            FOREIGN KEY(accountId) REFERENCES account(id) ON DELETE CASCADE)""")

                    database.execSQL(
                            """INSERT INTO new_email (id, messageId, threadId, fromAddress, replyTo, boundary, unread,
                                secure, content, bodyPreview, subject, delivered, unsentDate, date, metadataKey, isMuted, trashDate,
                                accountId)
                                SELECT id, messageId, threadId, fromAddress, replyTo, boundary, unread,
                                secure, content, bodyPreview, subject, delivered, unsentDate, date, metadataKey, isMuted, trashDate,
                                accountId
                                FROM email""")

                    database.execSQL("DROP TABLE email")
                    database.execSQL("ALTER TABLE new_email RENAME TO email")

                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS email_metadataKey_index ON email (metadataKey, accountId)")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS email_messageId_index ON email (messageId, accountId)")

                    database.execSQL("""ALTER TABLE label ADD COLUMN accountId INTEGER DEFAULT $accountId""")
                    database.execSQL("""UPDATE label SET accountId=NULL WHERE type='SYSTEM'""")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS  new_label (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            uuid TEXT NOT NULL,
                                            color TEXT NOT NULL,
                                            text TEXT NOT NULL,
                                            type TEXT NOT NULL,
                                            visible INTEGER NOT NULL,
                                            accountId INTEGER,
                                            FOREIGN KEY(accountId) REFERENCES account(id) ON DELETE CASCADE)""")
                    database.execSQL(
                            """INSERT INTO new_label (id, uuid, color, text, type, visible, accountId)
                                SELECT id, uuid, color, text, type, visible, accountId
                                FROM label""")

                    database.execSQL("DROP TABLE label")
                    database.execSQL("ALTER TABLE new_label RENAME TO label")

                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_label_uuid ON label (uuid)")

                    database.execSQL("""ALTER TABLE raw_identitykey ADD COLUMN accountId INTEGER DEFAULT $accountId""")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS  new_raw_identitykey (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            recipientId TEXT NOT NULL,
                                            deviceId INTEGER NOT NULL,
                                            byteString TEXT NOT NULL,
                                            accountId INTEGER NOT NULL,
                                            UNIQUE(recipientId, deviceId, accountId),
                                            FOREIGN KEY(accountId) REFERENCES account(id) ON DELETE CASCADE)""")
                    database.execSQL(
                            """INSERT INTO new_raw_identitykey (recipientId, deviceId, byteString, accountId)
                                SELECT recipientId, deviceId, byteString, accountId
                                FROM raw_identitykey""")

                    database.execSQL("DROP TABLE raw_identitykey")
                    database.execSQL("ALTER TABLE new_raw_identitykey RENAME TO raw_identitykey")


                    database.execSQL("""ALTER TABLE raw_prekey ADD COLUMN accountId INTEGER DEFAULT $accountId""")
                    database.execSQL("""ALTER TABLE raw_prekey ADD COLUMN preKeyId INTEGER DEFAULT 1""")
                    database.execSQL("""UPDATE raw_prekey SET preKeyId=id""")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS  new_raw_prekey (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            preKeyId INTEGER NOT NULL,
                                            byteString TEXT NOT NULL,
                                            accountId INTEGER NOT NULL,
                                            FOREIGN KEY(accountId) REFERENCES account(id) ON DELETE CASCADE)""")
                    database.execSQL(
                            """INSERT INTO new_raw_prekey (id, preKeyId, byteString, accountId)
                                SELECT id, preKeyId, byteString, accountId
                                FROM raw_prekey""")

                    database.execSQL("DROP TABLE raw_prekey")
                    database.execSQL("ALTER TABLE new_raw_prekey RENAME TO raw_prekey")

                    database.execSQL("""ALTER TABLE raw_session ADD COLUMN accountId INTEGER DEFAULT $accountId""")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS  new_raw_session (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            recipientId TEXT NOT NULL,
                                            deviceId INTEGER NOT NULL,
                                            byteString TEXT NOT NULL,
                                            accountId INTEGER NOT NULL,
                                            UNIQUE(recipientId, deviceId, accountId),
                                            FOREIGN KEY(accountId) REFERENCES account(id) ON DELETE CASCADE)""")
                    database.execSQL(
                            """INSERT INTO new_raw_session (recipientId, deviceId, byteString, accountId)
                                SELECT recipientId, deviceId, byteString, accountId
                                FROM raw_session""")

                    database.execSQL("DROP TABLE raw_session")
                    database.execSQL("ALTER TABLE new_raw_session RENAME TO raw_session")

                    database.execSQL("""ALTER TABLE raw_signedprekey ADD COLUMN accountId INTEGER DEFAULT $accountId""")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS  new_raw_signedprekey (
                                            id INTEGER PRIMARY KEY NOT NULL,
                                            byteString TEXT NOT NULL,
                                            accountId INTEGER NOT NULL,
                                            FOREIGN KEY(accountId) REFERENCES account(id) ON DELETE CASCADE)""")
                    database.execSQL(
                            """INSERT INTO new_raw_signedprekey (id, byteString, accountId)
                                SELECT id, byteString, accountId
                                FROM raw_signedprekey""")

                    database.execSQL("DROP TABLE raw_signedprekey")
                    database.execSQL("ALTER TABLE new_raw_signedprekey RENAME TO raw_signedprekey")

                    database.execSQL("""ALTER TABLE pendingEvent ADD COLUMN accountId INTEGER DEFAULT $accountId""")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS  new_pendingEvent (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            data TEXT NOT NULL,
                                            accountId INTEGER NOT NULL,
                                            FOREIGN KEY(accountId) REFERENCES account(id) ON DELETE CASCADE)""")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_pending_event_id ON new_pendingEvent (id)")
                    database.execSQL(
                            """INSERT INTO new_pendingEvent (id, data, accountId)
                                SELECT id, data, accountId
                                FROM pendingEvent""")

                    database.execSQL("DROP TABLE pendingEvent")
                    database.execSQL("ALTER TABLE new_pendingEvent RENAME TO pendingEvent")

                    database.execSQL("""CREATE TABLE IF NOT EXISTS  account_contact (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            accountId INTEGER NOT NULL,
                                            contactId INTEGER NOT NULL,
                                            FOREIGN KEY(accountId) REFERENCES account(id) ON DELETE CASCADE,
                                            FOREIGN KEY(contactId) REFERENCES contact(id) ON DELETE CASCADE)""")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_contact_account ON account_contact (accountId, contactId)")

                    database.execSQL(
                            """INSERT INTO account_contact (contactId, accountId)
                                SELECT DISTINCT id, $accountId
                                FROM contact""")
                    database.setTransactionSuccessful()
                }finally {
                    database.endTransaction()
                }
            }
        }
    }
}
