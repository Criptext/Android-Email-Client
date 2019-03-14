package com.criptext.mail.db.dao

import androidx.room.*
import com.criptext.mail.db.ContactTypes
import com.criptext.mail.db.models.AccountContact
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.EmailContact


@Dao
interface AccountContactDao {

    @Insert
    fun insert(accountContact: AccountContact)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(accountContacts: List<AccountContact>)

    @Query("""DELETE FROM account_contact WHERE accountId=:accountId""")
    fun nukeTable(accountId: Long)

}