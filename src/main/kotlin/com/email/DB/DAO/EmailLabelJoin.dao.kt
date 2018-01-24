package com.email.DB.DAO

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
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

    @Query("SELECT * FROM email INNER JOIN email_label ON email.id=email_label.emailId WHERE email_label.labelId=:labelId")
    fun getEmailsFromTag(labelId: Int) : List<Email>

    @Query("SELECT * FROM label INNER JOIN email_label ON label.id=email_label.labelId WHERE email_label.emailId=:emailId")
    fun getLabelsFromEmail(emailId: Int) : List<Label>

}