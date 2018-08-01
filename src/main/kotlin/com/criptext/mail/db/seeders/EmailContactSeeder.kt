package com.criptext.mail.db.seeders

import com.criptext.mail.db.ContactTypes
import com.criptext.mail.db.dao.EmailContactJoinDao
import com.criptext.mail.db.models.EmailContact

/**
 * Created by sebas on 2/7/18.
 */

class EmailContactSeeder {

    companion object {
        var emailContacts : List<EmailContact> = mutableListOf()

        fun seed(emailContactJoinDao: EmailContactJoinDao){
            emailContacts = emailContactJoinDao.getAll()
            emailContactJoinDao.deleteAll(emailContacts)
            emailContacts = mutableListOf()
            for (a in 1..4){
                emailContacts += fillEmailContacts(a)
            }
            emailContactJoinDao.insertAll(emailContacts)
        }


        private fun fillEmailContacts(iteration: Int): EmailContact {
            lateinit var emailContact: EmailContact
            when (iteration) {
                1 -> emailContact = EmailContact(
                        id = 0,
                        emailId = 1,
                        contactId = 1,
                        type = ContactTypes.FROM)
                2 -> emailContact = EmailContact(
                        id = 0,
                        emailId = 2,
                        contactId = 2,
                        type = ContactTypes.BCC)

                3 -> emailContact = EmailContact(
                        id = 0,
                        emailId = 1,
                        contactId = 3,
                        type = ContactTypes.CC)

                4 -> emailContact = EmailContact(
                        id = 0,
                        emailId = 3,
                        contactId = 4,
                        type = ContactTypes.FROM)
            }
            return emailContact
        }
    }
}
