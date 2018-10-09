package com.criptext.mail.db.dao

import android.arch.persistence.room.*
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label

/**
 * Created by sebas on 1/24/18.
 */

@Dao
interface EmailLabelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(emailLabel : EmailLabel)

    @Query("""SELECT email.* FROM email INNER JOIN email_label
            ON email.id=email_label.emailId WHERE email_label.labelId=:labelId""")
    fun getEmailsFromLabel(labelId: Long) : List<Email>

    @Query("""SELECT label.* FROM label INNER JOIN email_label
            ON label.id=email_label.labelId WHERE email_label.emailId=:emailId""")
    fun getLabelsFromEmail(emailId: Long) : List<Label>

    @Query("""SELECT count(*) FROM email LEFT JOIN email_label
            ON email.id=email_label.emailId WHERE email.threadId=:threadId AND email_label.labelId=:labelId""")
    fun getEmailCountInLabelByEmailId(threadId: String, labelId: Long) : Int

    @Query("""SELECT DISTINCT label.* FROM label INNER JOIN
        email_label ON label.id=email_label.labelId WHERE
        email_label.emailId IN (select id FROM email WHERE threadId=:threadId) """)
    fun getLabelsFromEmailThreadId(threadId: String) : List<Label>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(emailLabels : List<EmailLabel>)

    @Query("SELECT * FROM email_label")
    fun getAll() : List<EmailLabel>

    @Query("""SELECT * FROM email_label
        where emailId in (:emailIds)""")
    fun getAllForLinkFile(emailIds: List<Long>) : List<EmailLabel>

    @Delete
    fun deleteAll(emailLabels: List<EmailLabel>)

    @Query("""DELETE FROM email_label
        WHERE labelId= :labelId AND emailId=:emailId""")
    fun deleteByEmailLabelIds(labelId: Long, emailId: Long)

    @Query("""DELETE FROM email_label
        WHERE emailId in (:emailIds)""")
    fun deleteRelationByEmailIds(emailIds: List<Long>)

    @Query("""DELETE FROM email_label
        WHERE labelId= :labelId AND emailId in (:emailIds)""")
    fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>)

    @Query("""DELETE FROM email_label
        WHERE labelId in (:labelIds) AND emailId in (:emailIds)""")
    fun deleteRelationByLabelsAndEmailIds(labelIds: List<Long>, emailIds: List<Long>)

    @Query("DELETE from email_label WHERE emailId = :id")
    fun deleteByEmailId(id: Long)
}