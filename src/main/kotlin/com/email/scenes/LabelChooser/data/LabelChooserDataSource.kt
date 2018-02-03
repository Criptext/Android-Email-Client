package com.email.scenes.LabelChooser.data

import com.email.DB.MailboxLocalDB

/**
 * Created by sebas on 2/2/18.
 */

class LabelChooserDataSource(private val mailboxLocalDB: MailboxLocalDB) {

    fun getAllLabels(): List<LabelThread> {
        return mailboxLocalDB.getAllLabels()
    }

    fun createLabelEmailRelation(labelId: Int, emailId: Int) {
        return mailboxLocalDB.createLabelEmailRelation(labelId, emailId)
    }
}
