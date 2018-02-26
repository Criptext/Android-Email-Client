package com.email.DB.DAO

import android.arch.persistence.room.*
import com.email.DB.models.Email
import com.email.DB.models.EmailLabel
import com.email.DB.models.Label

/**
 * Created by sebas on 1/24/18.
 */

@Dao
interface EmailLabelJoinDao {

    @Insert
    fun insert(emailLabel : EmailLabel)

    @Query("SELECT email.* FROM email INNER JOIN email_label ON email.id=email_label.emailId WHERE email_label.labelId=:labelId")
    fun getEmailsFromLabel(labelId: Int) : List<Email>

    @Query("SELECT label.* FROM label INNER JOIN email_label ON label.id=email_label.labelId WHERE email_label.emailId=:emailId")
    fun getLabelsFromEmail(emailId: Int) : List<Label>

    @Insert
    fun insertAll(emailLabels : List<EmailLabel>)

    @Query("SELECT * FROM email_label")
    fun getAll() : List<EmailLabel>

    @Delete
    fun deleteAll(emailLabels: List<EmailLabel>)

    @Query("DELETE FROM email_label WHERE labelId= :labelId AND emailId=:emailId")
    fun deleteByEmailLabelIds(labelId: Int, emailId: Int)
}