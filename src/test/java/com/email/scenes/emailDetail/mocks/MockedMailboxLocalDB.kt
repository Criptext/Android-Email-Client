package com.email.scenes.emailDetail.mocks

import com.email.db.ContactTypes
import com.email.db.DeliveryTypes
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.Account
import com.email.db.models.Email
import com.email.db.models.Label
import com.email.scenes.labelChooser.data.LabelWrapper
import com.email.scenes.mailbox.data.EmailThread
import com.github.kittinunf.result.Result
import java.util.*

/**
 * Created by sebas on 3/29/18.
 */

class MockedMailboxLocalDB: MailboxLocalDB {

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

    override fun removeLabelsRelation(labels: List<Label>, emailId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteEmailThreads(emailThreads: List<EmailThread>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createLabelEmailRelation(labelId: Long, emailId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createLabelEmailSent(emailId: Long) {
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

    override fun addEmail(email: Email): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createLabelsForEmailInbox(insertedEmailId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createContacts(contactName: String?, contactEmail: String, insertedEmailId: Long, type: ContactTypes) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getEmailsFromMailboxLabel(labelTextTypes: MailFolders, oldestEmailThread: EmailThread?, offset: Int, rejectedLabels: List<Label>): List<EmailThread> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLabelsFromLabelType(labelTextTypes: List<MailFolders>): List<Label> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteRelationByEmailIds(emailIds: List<Long>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLabelFromLabelType(labelTextType: MailFolders): Label {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateEmailAndAddLabelSent(id: Long, threadId: String, key: String, date: Date, status: DeliveryTypes) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getExistingAccount(): Account {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUnreadCounterLabel(labelId: Long): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTotalCounterLabel(labelId: Long): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val getEmailThreadOperation: (threadId: String) -> Result<EmailThread, Exception>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.


}
