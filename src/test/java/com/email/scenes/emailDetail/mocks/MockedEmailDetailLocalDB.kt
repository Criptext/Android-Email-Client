package com.email.scenes.emailDetail.mocks

import com.email.db.EmailDetailLocalDB
import com.email.db.MailFolders
import com.email.db.models.Email
import com.email.db.models.EmailLabel
import com.email.db.models.FullEmail
import com.email.db.models.Label

/**
 * Created by sebas on 3/29/18.
 */

class MockedEmailDetailLocalDB: EmailDetailLocalDB {

    override fun getLabelsFromThreadId(threadId: String): List<Label> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteRelationByEmailIds(emailIds: List<Long>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLabelFromLabelType(labelTextType: MailFolders): Label {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createLabelEmailRelations(emailLabels: List<EmailLabel>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateUnreadStatus(emailIds: List<Long>, updateUnreadStatus: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFullEmailsFromThreadId(threadId: String): List<FullEmail> {
        return nextLoadedEmailItems!!
    }

    override fun unsendEmail(emailId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var nextLoadedEmailItems: List<FullEmail>? = null


}
