package com.email.scenes.composer.mocks

import com.email.db.DeliveryTypes
import com.email.db.dao.EmailDao
import com.email.db.models.Email
import java.util.*

/**
 * Created by gabriel on 4/24/18.
 */

class MockedEmailDao : EmailDao {

    override fun insertAll(emails: List<Email>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAll(): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLatestEmails(): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNotArchivedEmailThreads(): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getEmailThreadsFromMailboxLabel(starterDate: Date, rejectedLabels: List<Long>, selectedLabel: Long, offset: Int): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLatestEmailFromThreadId(threadId: String): Email {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAll(emails: List<Email>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toggleRead(id: Long, unread: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateEmail(id: Long, threadId: String, key: String, date: Date, status: DeliveryTypes) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(emails: List<Email>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getEmailsFromThreadId(threadId: String): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(email: Email): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeDeliveryType(id: Long, deliveryType: DeliveryTypes) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInitialEmailThreadsFromMailboxLabel(rejectedLabels: List<Long>, selectedLabel: Long, offset: Int): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTotalUnreadThreads(rejectedLabels: List<Int>, selectedLabel: Long): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTotalThreads(selectedLabel: Long): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}