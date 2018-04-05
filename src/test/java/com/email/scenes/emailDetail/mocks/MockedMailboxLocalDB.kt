package com.email.scenes.emailDetail.mocks

import com.email.db.ContactTypes
import com.email.db.LabelTextTypes
import com.email.db.MailboxLocalDB
import com.email.db.models.Email
import com.email.db.models.Label
import com.email.scenes.labelChooser.data.LabelWrapper
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 3/29/18.
 */

class MockedMailboxLocalDB: MailboxLocalDB {
    override fun createContacts(contacts: String, insertedEmailId: Int, type: ContactTypes) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getEmailsFromMailboxLabel(labelTextTypes: LabelTextTypes, oldestEmailThread: EmailThread?, offset: Int): List<EmailThread> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllEmailThreads(): List<EmailThread> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getArchivedEmailThreads(): List<EmailThread> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllLabelWrappers(): List<LabelWrapper> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllLabels(): List<Label> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNotArchivedEmailThreads(): List<EmailThread> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeLabelsRelation(labels: List<Label>, emailId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun seed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteEmailThreads(emailThreads: List<EmailThread>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createLabelEmailRelation(labelId: Int, emailId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateUnreadStatus(emailThreads: List<EmailThread>, updateUnreadStatus: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun moveSelectedEmailThreadsToSpam(emailThreads: List<EmailThread>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun moveSelectedEmailThreadsToTrash(emailThreads: List<EmailThread>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLabelsFromThreadIds(threadIds: List<String>): List<Label> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addEmail(email: Email): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun createLabelsForEmailInbox(insertedEmailId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
