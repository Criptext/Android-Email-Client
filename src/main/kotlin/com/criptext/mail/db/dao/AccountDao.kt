package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.models.Account
import java.util.*

/**
 * Created by sebas on 3/3/18.
 */


@Dao
interface AccountDao {

    @Insert
    fun insertAll(accounts : List<Account>)

    @Insert
    fun insert(account :Account)

    @Query("SELECT * FROM account")
    fun getAll() : List<Account>

    @Query("SELECT * FROM account WHERE recipientId=:recipientId")
    fun getAccountByRecipientId(recipientId: String) : Account?

    @Query("SELECT * FROM account WHERE recipientId=:recipientId AND domain=:domain")
    fun getAccount(recipientId: String, domain: String) : Account?

    @Query("SELECT * FROM account WHERE id=:id")
    fun getAccountById(id: Long) : Account?

    @Query("""UPDATE account
        SET
        hasCloudBackup=:googleDriveIsActive,
        wifiOnly=:wifiOnly,
        autoBackupFrequency=:backupFrequency
        WHERE id=:id""")
    fun setGoogleDriveActive(id: Long, googleDriveIsActive: Boolean, wifiOnly: Boolean, backupFrequency: Int)

    @Query("SELECT * FROM account WHERE isActive=1")
    fun getLoggedInAccount() : Account?

    @Query("SELECT * FROM account WHERE isLoggedIn=1")
    fun getLoggedInAccounts() : List<Account>

    @Query("UPDATE account SET isActive=0")
    fun updateActiveInAccount()

    @Query("UPDATE account SET lastTimeBackup=:lastBackupDate")
    fun updateLastBackupDate(lastBackupDate: Date)

    @Query("UPDATE account SET isActive=1 WHERE id=:id")
    fun updateActiveInAccount(id: Long)

    @Query("UPDATE account SET isLoggedIn=0 WHERE id=:id")
    fun logoutAccount(id: Long)

    @Query("DELETE FROM account")
    fun nukeTable()

    @Delete
    fun delete(account: Account)

    @Delete
    fun deleteAll(accounts: List<Account>)

    @Query("""DELETE FROM account
            WHERE id=:accountId""")
    fun deleteAccountById(accountId: Long)

    @Query("""DELETE FROM account
            WHERE recipientId=:recipientId AND domain=:domain""")
    fun deleteAccountByRecipientId(recipientId: String, domain: String)

    @Query("""DELETE FROM account
            WHERE recipientId in (:recipientIds) AND domain in (:domain)""")
    fun deleteAccountsByRecipientId(recipientIds: List<String>, domain: List<String>)

    @Query("""UPDATE account
            SET name=:name
            where recipientId=:recipientId AND domain=:domain""")
    fun updateProfileName(name: String, recipientId: String, domain: String)

    @Query("""UPDATE account
            SET type=:type
            where recipientId=:recipientId AND domain=:domain""")
    fun updateAccountType(type: AccountTypes, recipientId: String, domain: String)

    @Query("""UPDATE account
            SET blockRemoteContent=:blockRemoteContent
            where recipientId=:recipientId AND domain=:domain""")
    fun updateBlockRemoteContent(blockRemoteContent: Boolean, recipientId: String, domain: String)

    @Query("""UPDATE account
            SET jwt=:jwt
            where recipientId=:recipientId AND domain=:domain""")
    fun updateJwt(recipientId: String, domain: String, jwt: String)

    @Query("""UPDATE account
            SET refreshToken=:token
            where recipientId=:recipientId AND domain=:domain""")
    fun updateRefreshToken(recipientId: String, domain: String, token: String)

    @Query("""UPDATE account
            SET signature=:signature
            where id=:id""")
    fun updateSignature(id: Long, signature: String)

}
