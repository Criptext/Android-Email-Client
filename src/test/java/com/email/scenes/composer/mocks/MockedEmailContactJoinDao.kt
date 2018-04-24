package com.email.scenes.composer.mocks

import com.email.db.ContactTypes
import com.email.db.dao.EmailContactJoinDao
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.EmailContact

/**
 * Created by gabriel on 4/24/18.
 */
class MockedEmailContactJoinDao: EmailContactJoinDao {
    override fun getEmailsFromContact(contactId: String): List<Email> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContactsFromEmail(emailId: Long, contactType: ContactTypes): List<Contact> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insertAll(emailUsers: List<EmailContact>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAll(): List<EmailContact> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(emailContact: EmailContact) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAll(emailUsers: List<EmailContact>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}