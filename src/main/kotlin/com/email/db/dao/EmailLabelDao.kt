package com.email.db.dao

import android.arch.persistence.room.*
import com.email.db.models.Email
import com.email.db.models.EmailLabel
import com.email.db.models.Label

/**
 * Created by sebas on 1/24/18.
 */

@Dao
interface EmailLabelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(emailLabel : EmailLabel)

    @Query("""SELECT email.* FROM email INNER JOIN email_label
            ON email.id=email_label.emailId WHERE email_label.labelId=:labelId""")
    fun getEmailsFromLabel(labelId: Int) : List<Email>

    @Query("""SELECT label.* FROM label INNER JOIN email_label
            ON label.id=email_label.labelId WHERE email_label.emailId=:emailId""")
    fun getLabelsFromEmail(emailId: Int) : List<Label>

    @Query("""SELECT DISTINCT label.* FROM label INNER JOIN
        email_label ON label.id=email_label.labelId WHERE
        email_label.emailId IN (select id FROM email WHERE threadid=:threadId) """)
    fun getLabelsFromEmailThreadId(threadId: String) : List<Label>

    @Insert
    fun insertAll(emailLabels : List<EmailLabel>)

    @Query("SELECT * FROM email_label")
    fun getAll() : List<EmailLabel>

    @Delete
    fun deleteAll(emailLabels: List<EmailLabel>)

    @Query("""DELETE FROM email_label
        WHERE labelId= :labelId AND emailId=:emailId""")
    fun deleteByEmailLabelIds(labelId: Int, emailId: Int)

}