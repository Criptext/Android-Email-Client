package com.email.scenes.emailDetail.mocks

import com.email.db.DeliveryTypes
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.Account
import com.email.db.models.Email
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.scenes.mailbox.data.EmailThread
import com.github.kittinunf.result.Result
import java.util.*

/**
 * Created by sebas on 3/29/18.
 */

class MockedMailboxLocalDB: MailboxLocalDB {
    override fun getThreadsFromMailboxLabel(userEmail: String, labelTextTypes: MailFolders, oldestEmailThread: EmailThread?, limit: Int, rejectedLabels: List<Label>): List<EmailThread> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateUnreadStatus(emailThreads: List<EmailThread>, updateUnreadStatus: Boolean, rejectedLabels: List<Long>) {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun getCustomLabels(): List<Label> {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>) {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun getEmailsByThreadId(threadId: String, rejectedLabels: List<Long>): List<Email> {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun deleteThreads(threadIds: List<String>) {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun createLabelEmailRelations(emailLabels: List<EmailLabel>) {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun getLabelsFromThreadIds(threadIds: List<String>): List<Label> {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun addEmail(email: Email): Long {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun createLabelsForEmailInbox(insertedEmailId: Long) {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun getLabelsFromLabelType(labelTextTypes: List<MailFolders>): List<Label> {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun deleteRelationByEmailIds(emailIds: List<Long>) {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun getLabelFromLabelType(labelTextType: MailFolders): Label {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun updateEmailAndAddLabelSent(id: Long, threadId: String, key: String, date: Date, status: DeliveryTypes) {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun getExistingAccount(): Account {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun getUnreadCounterLabel(labelId: Long): Int {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun getTotalCounterLabel(labelId: Long): Int {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}
