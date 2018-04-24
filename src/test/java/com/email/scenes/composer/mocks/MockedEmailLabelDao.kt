package com.email.scenes.composer.mocks

import com.email.db.dao.EmailLabelDao
import com.email.db.models.Email
import com.email.db.models.EmailLabel
import com.email.db.models.Label

/**
 * Created by gabriel on 4/24/18.
 */
class MockedEmailLabelDao: EmailLabelDao {
    override fun insert(emailLabel: EmailLabel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getEmailsFromLabel(labelId: Long): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLabelsFromEmail(emailId: Long): List<Label> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLabelsFromEmailThreadId(threadId: String): List<Label> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insertAll(emailLabels: List<EmailLabel>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAll(): List<EmailLabel> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAll(emailLabels: List<EmailLabel>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteByEmailLabelIds(labelId: Long, emailId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteRelationByEmailIds(emailIds: List<Long>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}